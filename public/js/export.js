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
        open : function() {
            $("#jiraBase").val(jiraOptions.base);
            $("#jiraProj").val(jiraOptions.project);
        },
        buttons : {
            "Ok" : function() {
                var jiraBase = $("#jiraBase");
                var jiraProj = $("#jiraProj");
                var username = $("#username");
                var password = $("#password");
                jira(jiraBase, jiraProj, username, password);
                $(this).dialog("close");
            },
            "Cancel" : function() {
                $(this).dialog("close");
            }
        }
    });
});

function jira(jiraBase, project, username, password) {
    var jiraREST = jiraBase + "/rest/"
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
    // required values from the epic 'feature' creation
    var epic_id;
    var epic_key;
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
            "labels" : getFeatureTags(),
            "customfield_10004" : getFeatureTitle()
            // TODO - put in background steps
        }
    }, function(data) {
        epic_id = data.id;
        epic_key = data.key;
        // for each scenario
        $('.scenario').each(function() {
            // required values from the epic 'feature' creation
            var test_case_id;
            var test_case_key;
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
                    "customfield_10001" : epic_key
                }
            }, function(data) {
                test_case_id = data.id;
                test_case_key = data.key;
                // add the test steps
                $.each(getScenarioTestSteps($(this)), function(key, step) {
                    $.post(jiraREST + "zapi/latest/teststep/" + test_case_id, {
                        "step" : step
                    }, function() {
                    }, 'json');
                });
                // TODO - do something with the example data - will want a
                // custom field
                // for this

            }, 'json');
        });

    }, 'json');
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