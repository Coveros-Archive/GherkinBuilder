var scenarioCount;

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
            $("#jiraProj").val(jiraOptions.project);
            checkInputs();
            $("#jira-creds").keyup(function(e) {
                checkInputs();
                if (e.keyCode == $.ui.keyCode.ENTER && checkInputs()) {
                    $(this).next().find("button:eq(0)").trigger("click");
                }
            });
        },
        buttons : {
            "Ok" : function() {
                $(this).next().find("button:eq(0)").button("disable");
                var jiraProj = $("#jiraProj").val();
                var auth = btoa($("#username").val() + ":" + $("#password").val());
                $('#error-messages').empty();
                jira(jiraProj, auth);
            },
            "Cancel" : function() {
                $(this).dialog("close");
            }
        }
    });
});

function checkInputs() {
    $('#jira-creds input').each(function() {
        if ($(this).val() == "") {
            $(this).addClass("red");
        } else {
            $(this).removeClass("red");
        }
    });
    // check to see if all required things are filled in
    if ($('.red').length) {
        $("#jira-creds").next().find("button:eq(0)").button("disable");
        return false;
    } else {
        $("#jira-creds").next().find("button:eq(0)").button("enable");
        return true;
    }
}

function jiraSuccess(epic_link) {
    $('#success-messages').empty().html("Successfully created everything, forwarding you to JIRA");
    window.location.href = epic_link;
}

function jira(project, auth) {
    // required values from the epic 'feature' creation
    var epic_key;
    var epic_link;
    // create the epic to contain the scenarios
    $.post("api/createFeature.php", {
        "auth" : auth,
        "project" : project,
        "featureTags" : getFeatureTags(),
        "featureTitle" : getFeatureTitle(),
        "featureDescription" : getFeatureDescription(),
    }, function(data) {
        epic_key = data.key;
        epic_link = data.self.split("/rest/")[0] + "/browse/" + epic_key;
        // for each scenario
        scenarioCount = $('.scenario').length;
        var thisCount = 0;
        var anyFailures = false;
        $('.scenario').each(function() {
            // get scenarioTitle
            var scenarioTitle = getScenarioTitle($(this));
            // create the test case
            $.post("api/createScenario.php", {
                "auth" : auth,
                "project" : project,
                "feature" : epic_key,
                "scenarioTags" : getScenarioTags($(this)),
                "scenarioTitle" : scenarioTitle,
                "scenarioDescription" : getScenarioDescription($(this)),
                "backgroundSteps" : getBackgroundTestSteps(),
                "scenarioTestSteps" : getScenarioTestSteps($(this)),
                "scenarioExamples" : getScenarioExamples($(this)),
            }).done(function() {
                $('#success-messages').html($('#success-messages').html() + "<br/>Successfully create Scenario: " + scenarioTitle);
            }).fail(function(xhr) {
                $('#error-messages').html(xhr.responseText);
                anyFailures = true;
            }).always(function() {
                thisCount++;
                if (thisCount === scenarioCount) {
                    if (anyFailures) {
                        $('#jira-creds').next().find("button:eq(0)").button("enable");
                    } else {
                        jiraSuccess(epic_link);
                    }
                }
            });
        });
        if (!$('.scenario').length) {
            jiraSuccess(epic_link);
        } else {
            $('#success-messages').html("Once all scenario are complete, <a href='" + epic_link + "'>see created test cases on JIRA</a>");
        }
    }, 'json').fail(function(xhr) {
        $('#error-messages').html(xhr.responseText);
        $('#jira-creds').next().find("button:eq(0)").button("enable");

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
            // add the required space
            if ($(this).hasClass('blue')) {
                step += " ";
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
            // add the required space
            if ($(this).hasClass('blue')) {
                step += " ";
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
