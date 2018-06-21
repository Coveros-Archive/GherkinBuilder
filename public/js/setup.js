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

if (typeof String.prototype.startsWith !== 'function') {
    String.prototype.startsWith = function(str) {
        return this.slice(0, str.length) === str;
    };
}
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(str) {
        return this.slice(-str.length) === str;
    };
}
if (typeof String.prototype.stripTags !== 'function') {
    String.prototype.stripTags = function() {
        var tmp = document.createElement("DIV");
        tmp.innerHTML = this;
        return tmp.textContent || tmp.innerText || "";
    }
}
function rand(length) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < length; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    return text;
}
function resizeInput() {
    // will resize our inputs to the input value size
    $(this).attr('size', $(this).val().length ? $(this).val().length : $(this).attr('placeholder') ? $(this).attr('placeholder').length : '20');
}
$(document).ready(function() {
    // setup the text areas to make easily readable
    $('textarea').attr('rows', '1');
    makeDynamic();
    fillTag($('#featTag'));

    // setup our buttons
    $('#addBackgroundStep').click(function() {
        addTestStep(this);
    }).button();
    $('#addScenario').click(function() {
        addScenario();
        $('.required').each(function() {
            checkRequired($(this));
        });
    }).button();
    $('#exportFile').click(function() {
        download();
    }).button().button("disable");
    $('#exportJIRA').click(function() {
        getJIRACreds();
    }).button().button("disable");
    $('button[name=linkButton]').button().button("disable");
});
