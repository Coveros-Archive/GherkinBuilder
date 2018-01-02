function download() {
    var data = "";
    // get feature information
    data += getFeatureTags().join(" ") + "\n";
    data += "Feature: " + getFeatureTitle() + "\n";
    data += getFeatureDescription() + "\n\n";
    // get background steps
    data += "Background: " + getBackgroundTitle() + "\n";
    data += getBackgroundDescription() + "\n";
    data += getBackgroundTestSteps().join("\n") + "\n\n";
    // get scenario information
    $('.scenario').each(function() {
        data += getScenarioTags($(this)).join(" ") + "\n";
        data += getScenarioType($(this)) + " ";
        data += getScenarioTitle($(this)) + "\n";
        data += getScenarioDescription($(this)) + "\n";
        data += getScenarioTestSteps($(this)).join("\n") + "\n";
        $.each(getScenarioExamples($(this)), function(key, example) {
            if (Object.prototype.hasOwnProperty.call(example, "tags")) {
                data += example.tags.join(" ") + "\n";
            }
            data += "Examples:\n";
            data += "| " + example.inputs.join(" | ") + " |\n";
            $.each(example.data, function(key, example) {
                data += "| " + Object.values(example).join(" | ") + " |";
            });
        });
    });
    // fix brackets
    data = data.replace(/</g, '&lt;');
    data = data.replace(/>/g, '&gt;');
    // fix extra spaces
    data = data.replace(/&nbsp;/g, ' ');
    data = data.replace(/( ){2,}/g, ' ');
    // download the file
    document.location = 'data:Application/octet-stream,' + encodeURIComponent(data);
    // warn the user about filename and linebreaks
    $("#download").dialog("open");
}
$(function() {
    $("#download").dialog({
        autoOpen : false,
        modal : true,
        buttons : {
            "Ok" : function() {
                $(this).dialog("close");
            }
        }
    });
    $("#jira-creds").dialog({
        autoOpen : false,
        modal : true,
        buttons : {
            "Ok" : function() {
                var username = $("#username");
                var password = $("#password");
                jira(username, password);
                $(this).dialog("close");
            },
            "Cancel" : function() {
                $(this).dialog("close");
            }
        }
    });
});

function jira(username, password) {
    // create an epic for the feature, and then add each scenario/scenario
    // outline into it
    // the background steps will be added into the epic

    // define the jira we are interacting with - hard coded for now
    var jiraBase = "https://agile.vignetcorp.com:8085/jira";
    var jiraREST = jiraBase + "/rest/"
    // define the project we are entering code into - hard coded for now,
    // consider making this a passable parameter
    var project = "ATA";
    // setup our default credentials
    $.ajaxSetup({
        beforeSend : function(xhr) {
            // TODO - still an issue
            xhr.setRequestHeader('Access-Control-Allow-Origin', 'http://localhost');
        },
        contentType : "application/json; charset=UTF-8",
        dataType : "json",
        crossDomain : true,
        xhrFields : {
            withCredentials : true
        },
        username : username,
        password : password
    });
    // create the epic to contain the scenarios
    $.post(jiraREST + "api/2/issue", {
        "fields" : {
            "project" : {
                "key" : project
            },
            "summary" : getFeatureTitle(),
            "description" : getFeatureDescription(),
            "issuetype" : {
                "name" : "Epic"
            },
            "labels" : getFeatureTags()
        // TODO - put in background steps
        }
    }, function(d) {
        // TODO - get the epic id
        // $('body').html(d.status);
    }, 'json');
    // for each scenario
    $('.scenario').each(function() {
        // create the test case
        $.post(jiraREST + "api/2/issue", {
            "fields" : {
                "project" : {
                    "key" : project
                },
                "summary" : getScenarioTitle($(this)),
                "description" : getScenarioDescription($(this)),
                "issuetype" : {
                    "name" : "Test"
                },
                "labels" : getScenarioTags($(this)),
                "customfield_10006" : epid_id
            }
        }, function(d) {
            // TODO - get the test case id
            // $('body').html(d.status);
        }, 'json');
        // add the test steps
        $.each(getScenarioTestSteps($(this)), function(key, step) {
            $.post(jiraREST + "zapi/latest/teststep/" + test_case_id, {
                "step" : step
            }, function(d) {
                // $('body').html(d.status);
            }, 'json');
        });
        // TODO - do something with the example data - will want a custom field
        // for this
    });
}

function getJIRACreds() {
    $("#jira-creds").dialog("open");
}

function getFeatureTags() {
    var tags = [];
    if ($('#featTag').val() != "") {
        tags = $('#featTag').val().split(" ");
    }
    return tags;
}

function getFeatureTitle() {
    return $('#featuredef input').val();
}

function getFeatureDescription() {
    var def = "";
    if ($('#featuredef textarea').val() != "") {
        def = $('#featuredef textarea').val();
    }
    return def;
}

function getBackgroundTitle() {
    return $('#backgrounddef input').val();
}

function getBackgroundDescription() {
    var def = "";
    if ($('#backgrounddef textarea').val() != "") {
        def = $('#backgrounddef textarea').val();
    }
    return def;
}

function getBackgroundTestSteps() {
    var steps = [];
    $("#backgrounddef").find('.testStep').each(function() {
        var step = "";
        $(this).children('input,select,span').each(function() {
            if (($(this).val() == "" || $(this).val() == null) && ($(this).is("input") || $(this).is("select"))) {
                if ($(this).attr('placeholder') !== undefined) {
                    step += $(this).attr('placeholder');
                }
            } else if ($(this).is('select') || $(this).is('input')) {
                if ($(this).val() !== undefined) {
                    step += $(this).val();
                }
            } else {
                if ($(this).html() !== undefined) {
                    step += $(this).html().stripTags();
                }
            }
        });
        steps.push(step);
    });
    return steps;
}

function getScenario(element) {
    var scenario = {};
    scenario.tags = getScenarioTags(element);
    scenario.type = getScenarioType(element);
    scenario.title = getScenarioTitle(element);
    scenario.description = getScenarioDescription(element);
    scenario.steps = getScenarioTestSteps(element);
    return scenario;
}

function getScenarioTags(element) {
    var tags = [];
    if ($(element).children('input.purple').val() != "") {
        tags = $(element).children('input.purple').val().split(" ");
    }
    return tags;
}

function getScenarioType(element) {
    return $(element).children('div.green').children('span.what').html();
}

function getScenarioTitle(element) {
    return $(element).children('div.green').children('input.green').val();
}

function getScenarioDescription(element) {
    var def = "";
    if ($(element).children('div.green').children('textarea').val() != "") {
        def = $(element).children('div.green').children('textarea').val();
    }
    return def;
}

function getScenarioTestSteps(element) {
    var steps = [];
    $(element).find('.testStep').each(function() {
        var step = "";
        $(this).children('input,select,span').each(function() {
            if (($(this).val() == "" || $(this).val() == null) && ($(this).is("input") || $(this).is("select"))) {
                if ($(this).attr('placeholder') !== undefined) {
                    step += $(this).attr('placeholder');
                }
            } else if ($(this).is('select') || $(this).is('input')) {
                if ($(this).val() !== undefined) {
                    step += $(this).val();
                }
            } else {
                if ($(this).html() !== undefined) {
                    step += $(this).html().stripTags();
                }
            }
        });
        steps.push(step);
    });
    return steps;
}

function getScenarioExamples(element) {
    var examples = [];
    if ($(element).children('.examples').length) {
        $($(element).children('.examples')).each(function() {
            var example = {};
            if ($(this).children('input.purple').val() != "") {
                example.tags = $(this).children('input.purple').val().split(" ");
            }
            var inputs = [];
            $(this).find('th').each(function() {
                inputs.push($(this).html());
            });
            example.inputs = inputs;
            var data = [];
            $(this).find('tbody tr').each(function() {
                var dataSet = {};
                $(this).find('input,select').each(function(index) {
                    dataSet[inputs[index]] = $(this).val();
                });
                data.push(dataSet);
            });
            example.data = data;
            examples.push(example);
        });
    }
    return examples;
}