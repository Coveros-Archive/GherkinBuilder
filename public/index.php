<?php
$params = parse_ini_file ( __DIR__ . DIRECTORY_SEPARATOR . "props.ini" );
$useJIRA = false;
if ($params ['base'] != "") {
    $useJIRA = true;
}
?>

<html>
<head>
<title>Gherkin Builder</title>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>
<script src="https://harvesthq.github.io/chosen/chosen.jquery.js"></script>
<script src="js/getSteps.js?<?php echo time() ?>"></script>
<script src="js/steps.js?<?php echo time() ?>"></script>
<script src="js/setup.js?<?php echo time() ?>"></script>
<script src="js/buildGherkin.js?<?php echo time() ?>"></script>
<script src="js/export.js?<?php echo time() ?>"></script>
<?php if ($useJIRA) { ?>
<script src="js/jira.js?<?php echo time() ?>"></script>
<?php
}
if (file_exists ( "js/tags.js" )) {
?>
<script src="js/tags.js?<?php echo time() ?>"></script>
<?php } ?>
<script src="props.js?<?php echo time() ?>"></script>
<script>
            console.log(testSteps);
        </script>

<link rel="stylesheet"
    href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/themes/base/jquery-ui.min.css">
<link rel="stylesheet"
    href="https://harvesthq.github.io/chosen/chosen.css">
<link rel="stylesheet" href="css/default.css?<?php echo time() ?>">
<link rel="stylesheet"
    href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
</head>
<body>
    <div id='feat'>
        <?php if ($useJIRA) { ?>
        <span class="edit" style="position: inherit;"
            onclick="existingFeature()" title="Use Existing Feature in JIRA"> <i
            class="fa fa-pencil-square-o"></i>
        </span> <span id="featLink" class="link" style="position: inherit;"
            onclick="link(this)" title="Link Feature to Issue in JIRA"><i
            class='fa fa-link'></i></span>
        <?php } ?>
        <input id='featTag' class='purple small'
            placeholder='Feature Tags' />
        <?php if ($useJIRA) { ?>
        <input id='jiraFeat' class='green small required red'
            placeholder='Existing JIRA Epic Key' style='display: none;' required />
        <?php } ?>
        <div id='featuredef' class='green'>
            Feature: <input class='green small required red'
                placeholder='Feature Title' type='text' required /> <br />
            <textarea class='green' placeholder="User Story"></textarea>
        </div>
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
            <button id="addScenario" class="ui-button">Add Scenario</button>
            <button id="exportFile" class="ui-button">Export as Feature File</button>
            <?php if ($useJIRA) { ?>
            <button id="exportJIRA" class="ui-button" disabled>Export to
                JIRA</button>
            <?php } ?>
            <?php foreach ($params ['links'] as $title => $link) {
                echo "<button name='linkButton' class='ui-button' onclick='getLinkCreds(\"$link\")'>$title</button> ";
            } ?>
        </p>
    </div>

    <div id="download" title="Download Feature">
        <p>Note: this file will need to be renamed.</p>
        <p>Open in wordpad or np++ to preserve line breaks.</p>
    </div>

    <?php if ($useJIRA) { ?>
    <div id="jira-creds" class="creds" title="JIRA Credentials">
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
        <div
            style="text-align: justify; font-style: italic; font-size: small;">Note
            that attempting to export to JIRA multiple times will result in
            duplicate test suites and cases. Please be judicious in your usage of
            this capability.</div>
        <div id="error-messages" class="error" style="text-align: center"></div>
        <div id="success-messages" class="green" style="text-align: center"></div>
    </div>
    <?php } ?>

    <?php if (!empty ( $params ['links'])) { ?>
        <div id="link-creds" class="creds" title="Credentials">
            <form>
                <div>
                    <label for="user">Username</label> <input type="text"
                        name="user" id="user" required />
                </div>
                <div>
                    <label for="pass">Password</label> <input type="password"
                        name="pass" id="pass" required />
                </div>
                <div>
                    <input type="hidden" name="link" id="link" required />
                </div>
            </form>
            <div
                style="text-align: justify; font-style: italic; font-size: small;">Note
                that attempting to send this data multiple times may result in
                duplicate test suites and cases. Please be judicious in your usage of
                this capability.</div>
            <div id="error-mess" class="error" style="text-align: center"></div>
            <div id="success-mess" class="green" style="text-align: center"></div>
        </div>
        <?php } ?>

    <div id="delete" title="Confirmation Required">Are you sure you want
        to delete this?</div>
</body>
</html>