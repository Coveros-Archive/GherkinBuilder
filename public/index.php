<!-- 
Improvements
 * Delete Data Row
 * Delete Data Table
 * Insert Tests
 * Insert Test Steps
 * Insert Data Row
 * When using inputs, allow them to double up (use same data column)
 ** Keep Data Table columns in order with test steps
 * Remove drop down form formatting, or add back formatting for input areas
 ** resize min-width for multidropdown to placeholder length
 *** When using autocomplete, and clicking off (not selecting), multiselect does not get filled out
 *** Export is displaying 'dimmed' opt values
 * When writing a step, and 'createStep' is used, and fields don't get filled in with values, the input fields get those values
 -->
 
<html>
    <head>
        <title>Cucumber Parser</title>
        <script src="https://code.jquery.com/jquery-1.10.2.js"></script>
        <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
        <script src="https://harvesthq.github.io/chosen/chosen.jquery.js"></script>
        <script src="js/getSteps.js"></script>
        <script src="js/steps.js"></script>
        <script src="js/setup.js"></script>
        <script src="js/buildGherkin.js"></script>
        <script src="js/export.js"></script>
        <script src="props.js"></script>
        <script>
            console.log(testSteps);
        </script>
        
        <link rel="stylesheet"
            href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <link rel="stylesheet"
            href="https://harvesthq.github.io/chosen/chosen.css">
        <link rel="stylesheet" href="css/default.css">
    </head>
    <body>
        <input id='featTag' class='purple small' placeholder='Feature Tags' />
        <div id='featuredef' class='green'>
            Feature: <input class='green small required red'
                placeholder='Feature Title' type='text' required /> <br />
            <textarea class='green' placeholder="User Story"></textarea>
        </div>
        <div id='backgrounddef' class='background'>
            <div class="green small">
                <span class='what'>Background:</span> <input class='green small'
                    placeholder='Background Title' type='text' /> <br />
                <textarea rows='1' placeholder='Description'></textarea>
            </div>
            <div class='testSteps'></div>
            <button id="addBackgroundStep"
                class="ui-button ui-widget ui-corner-all ui-button-small">Add
                Background Step</button>
        </div>
        <div id='tests'></div>
        <div style="position: fixed; bottom: 0px; right: 0px;">
            <p style="text-align: center;">
                <button id="addScenario" class="ui-button">Add
                    Scenario</button>
                <button id="exportFile" class="ui-button">Export
                    as Feature File</button>
                <button id="exportJIRA"
                    class="ui-button" disabled>Export
                    to JIRA</button>
            </p>
        </div>
    
        <div id="download" title="Download Feature">
            <p>Note: this file will need to be renamed.</p>
            <p>Open in wordpad or np++ to preserve line breaks.</p>
        </div>
        <div id="jira-creds" title="JIRA Credentials">
            <form>
                <div>
                    <label for="jiraProj">JIRA Project</label> <input type="text"
                        name="jiraProj" id="jiraProj" required />
                </div>
                <div>
                    <label for="username">Username</label> <input type="text"
                        name="username" id="username" required />
                </div>
                <div>
                    <label for="password">Password</label> <input type="password"
                        name="password" id="password" required />
                </div>
            </form>
            <div id="error-messages" class="error" style="text-align:center"></div>
            <div id="success-messages" class="green" style="text-align:center"></div>
        </div>
        <div id="delete" title="Confirmation Required">
            Are you sure you want to delete this test step?
        </div>
    </body>
</html>