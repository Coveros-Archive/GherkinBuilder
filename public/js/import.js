/*
 * Copyright 2018 Coveros, Inc.
 *
 * This file is part of Gherkin Builder.
 *
 * Gherkin Builder is licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

$(function() {
    var url = new URL(window.location.href);
    var feature = url.searchParams.get("Feature");
    var scenarios = url.searchParams.get("Scenarios");

    // if we have feature information
    if (feature !== null) {
        var featureJson = JSON.parse(feature);
        console.log(featureJson);
        // if there are tags, do something
        if (featureJson.hasOwnProperty('featureTags') && featureJson.featureTags.length > 0) {
            featureJson.featureTags.forEach(function(tag) {
                $('#featTag').val(tag).blur();
            });
        }
        // of there are links, do something
        if (featureJson.hasOwnProperty('featureLinks') && featureJson.featureLinks.length > 0) {
            fillLink($('#featLink'));
            featureJson.featureLinks.forEach(function(link) {
                $('#featLink').val(link).blur();
            });
        }
        // add in the title
        if (featureJson.hasOwnProperty('featureTitle') && featureJson.featureTitle !== "") {
            $('#featuredef input').val(featureJson.featureTitle);
        }
        // add in the description
        if (featureJson.hasOwnProperty('featureDescription') && featureJson.featureDescription !== "") {
            $('#featuredef textarea').val(featureJson.featureDescription);
        }
        // add in the background title
        if (featureJson.hasOwnProperty('backgroundTitle') && featureJson.backgroundTitle !== "") {
            $('#backgrounddef input').val(featureJson.backgroundTitle);
        }
        // add in the background description
        if (featureJson.hasOwnProperty('backgroundDescription') && featureJson.backgroundDescription !== "") {
            $('#backgrounddef textarea').val(featureJson.backgroundDescription);
        }
        // add in background steps
        if (featureJson.hasOwnProperty('backgroundSteps') && featureJson.backgroundSteps.length > 0) {
            featureJson.backgroundSteps.forEach(function(backgroundStep) {
                $('#addBackgroundStep').click();
                var stepEl = $('#backgrounddef .test-step:last-child');
                stepEl.find('.blue').val(backgroundStep.step.split(/ /)[0]).trigger("chosen:updated");
                stepEl.find('input').val(backgroundStep.step.split(/ (.+)/)[1]).blur();
            });
        }
        // finally, add our feature key if it exists
        if (featureJson.hasOwnProperty('featureKey') && featureJson.featureKey !== "") {
            existingFeature();
            $('#jiraFeat').val(featureJson.featureKey);
        }
    }

    // if we have scenario information
    if (scenarios !== null) {
        var scenariosJson = JSON.parse(scenarios);
        scenariosJson.forEach(function(scenario) {
            console.log(scenario);
            // add our feature key if it exists
            if (scenario.hasOwnProperty('featureKey') && scenario.featureKey !== "") {
                existingFeature();
                $('#jiraFeat').val(scenario.featureKey);
            }
            // add our scenario
            if (scenario.hasOwnProperty('scenarioKey') && scenario.scenarioKey !== "") {
                addScenario(scenario.scenarioKey);
            } else {
                addScenario();
            }
            var scenarioEl = $('#tests .scenario:last-child');
            // if there are tags, do something
            if (scenario.hasOwnProperty('scenarioTags') && scenario.scenarioTags.length > 0) {
                scenario.scenarioTags.forEach(function(tag) {
                    addTag(scenarioEl.find('input.purple'), tag);
                });
            }
            // of there are links, do something
            if (scenario.hasOwnProperty('scenarioLinks') && scenario.scenarioLinks.length > 0) {
                fillLink($('#featLink'));
                scenario.scenarioLinks.forEach(function(link) {
                    addLink(scenarioEl.find('input.jiralink'), link)
                });
            }
            // add in the title
            if (scenario.hasOwnProperty('scenarioTitle') && scenario.scenarioTitle !== "") {
                scenarioEl.find('input.green').val(scenario.scenarioTitle);
            }
            // add in the description
            if (scenario.hasOwnProperty('scenarioDescription') && scenario.scenarioDescription !== "") {
                scenarioEl.find('textarea').val(scenario.scenarioDescription);
            }
            // add in test steps
            if (scenario.hasOwnProperty('scenarioTestSteps') && scenario.scenarioTestSteps.length > 0) {
                scenario.scenarioTestSteps.forEach(function(testStep) {
                    scenarioEl.find('button').click();
                    var stepEl = scenarioEl.find('.test-step:last-child');
                    stepEl.find('.blue').val(testStep.step.split(/ /)[0]).trigger("chosen:updated");
                    stepEl.find('input').val(testStep.step.split(/ (.+)/)[1]).blur();
                });
            }
            // add in examples
            if (scenario.hasOwnProperty('scenarioExamples') && scenario.scenarioExamples.length > 0) {
                scenarioEl.find('.examples').remove();
                scenario.scenarioExamples.forEach(function(example) {
                    scenarioEl.find('.addTable').click();
                    var exampleTable = scenarioEl.find('.examples:last-child');
                    if (example.hasOwnProperty('tags') && example.tags.length > 0) {
                        example.tags.forEach(function(tag) {
                            addTag(exampleTable.find('input.purple'), tag)
                        })
                    }
                    exampleTable.find('tbody tr').remove();
                    if (example.hasOwnProperty('data') && example.data.length > 0) {
                        example.data.forEach(function(data) {
                            exampleTable.find('button').click();
                            Object.keys(data).forEach(function(key) {
                                var cell = exampleTable.find('thead th:contains(' + key + ')');
                                var col = cell.parent().children().index(cell);
                                var exampleTableRow = exampleTable.find('tbody tr:last-child');
                                exampleTableRow.find('td:nth-child(' + (col + 1) + ') select').val(data[key]).trigger("chosen:updated");
                            });
                        })
                    }
                })
            }
        });
    }

    // fix all of our inputs
    makeDynamic();
    $('.required').each(function() {
        checkRequired($(this));
    });
});
