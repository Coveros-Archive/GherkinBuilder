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
    fillLink($('#featLink'));
});

function existingFeature() {
    if ($("#jiraFeat").is(":visible")) {
        $("#featLink").show();
        $("#featTag").show();
        $("#featuredef").show();
        $("#jiraFeat").hide();
        $("#exportFile").button("enable");
        $('.link').show();
        $('.tag').show();
    } else {
        $("#featLink").hide();
        $("#featTag").hide();
        $("#featuredef").hide();
        $("#jiraFeat").show();
        $("#exportFile").button("disable");
        $('.link').hide();
        $('.tag').hide();
    }
    checkRequired($("#jiraFeat"));
}

function fillLink(el) {
    $(el).keyup(function(e) {
        if (e.keyCode === 32 || e.keyCode === 13) {
            addLink(el);
        }
    }).blur(function() {
        addLink(el);
    });
}
function addLink(el, link) {
    if (link === "" || link === undefined) {
        link = $(el).val();
        if (link === "") {
            return;
        }
    }
    // build the environment
    var span = $("<span>");
    span.html(link);
    span.addClass('link');
    span.click(function() {
        $(this).remove();
    });
    $(el).after(span);
    $(el).val("");
}
