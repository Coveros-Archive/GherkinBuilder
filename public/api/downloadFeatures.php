<!--
Copyright 2018 Coveros, Inc.

This file is part of Gherkin Builder.

Gherkin Builder is licensed under the Apache License, Version
2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy
of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
-->

<?php
$params = parse_ini_file ( dirname ( __DIR__ ) . DIRECTORY_SEPARATOR . "props.ini" );

$ERROR = "HTTP/1.1 500 Internal Server Error";
$CONTENTTYPE = "Content-Type: application/json";
$USERNAME = "username";
$PASSWORD = "password";
$PROJECT = "project";
$DESCRIPTION = "description";

// setup our directory to host our files
$directory = '../tmp/';
if (! file_exists ( $directory )) {
    mkdir ( $directory, 0777, true );
}

// get our jira creds
if (! isset ( $_GET [$USERNAME] ) || $_GET [$USERNAME] == "") {
    echo "Authorization not provided";
    header ( $ERROR );
    exit ();
} else {
    $username = $_GET [$USERNAME];
}
if (! isset ( $_GET [$PASSWORD] ) || $_GET [$PASSWORD] == "") {
    echo "Authorization not provided";
    header ( $ERROR );
    exit ();
} else {
    $password = $_GET [$PASSWORD];
}
$project = $params [$PROJECT];
if (isset ( $_GET [$PROJECT] ) || $_GET [$PROJECT] != "") {
    $project = $_GET [$PROJECT];
}

// setup our zip files
$zip = $directory . "Features-" . time () . ".zip";

// get all of the feature files
$ch = curl_init ();
curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/search?jql=project=" . $project . "%20AND%20labels=Feature&fields=key&maxResults=9999" );
curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
curl_setopt ( $ch, CURLOPT_USERPWD, "$username:$password" );
curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
        $CONTENTTYPE 
) );
$response = curl_exec ( $ch );
curl_close ( $ch );
// check for errors
if (! is_object ( json_decode ( $response ) )) {
    header ( $ERROR );
    exit ();
}
$issues = json_decode ( $response, true ) ['issues'];

foreach ( $issues as $issue ) {
    $file = $issue ['key'] . ".feature";
    // get the feature content
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/issue/" . $issue ['key'] );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
    curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
    curl_setopt ( $ch, CURLOPT_USERPWD, "$username:$password" );
    curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
            $CONTENTTYPE 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    // check for errors
    if (! is_object ( json_decode ( $response ) )) {
        header ( $ERROR );
        exit ();
    }
    $feature = json_decode ( $response, true ) ['fields'];
    // start adding feature content
    $content = "@Feature_" . $issue ['key'];
    foreach ( $feature ['labels'] as $label ) {
        if ($label != "Feature") {
            $content .= " @" . $label;
        }
    }
    $content .= "\nFeature: " . $feature ['summary'];
    $content .= "\n  " . implode ( "\n  ", explode ( "\n", $feature [$DESCRIPTION] ) );
    // get all scenario details
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/search?jql=project=" . $project . "%20AND%20\"Epic%20Link\"=" . $issue ['key'] . "&fields=key&maxResults=9999" );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
    curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
    curl_setopt ( $ch, CURLOPT_USERPWD, "$username:$password" );
    curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
            $CONTENTTYPE 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    // check for errors
    if (! is_object ( json_decode ( $response ) )) {
        header ( $ERROR );
        exit ();
    }
    $isswes = json_decode ( $response, true ) ['issues'];
    foreach ( $isswes as $isswe ) {
        $ch = curl_init ();
        curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/issue/" . $isswe ['key'] );
        curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
        curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
        curl_setopt ( $ch, CURLOPT_USERPWD, "$username:$password" );
        curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
        curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
                $CONTENTTYPE 
        ) );
        $response = curl_exec ( $ch );
        curl_close ( $ch );
        // check for errors
        if (! is_object ( json_decode ( $response ) )) {
            header ( $ERROR );
            exit ();
        }
        $scenario = json_decode ( $response, true ) ['fields'];
        // add scenario content
        $content .= "\n\n  @" . $isswe ['key'];
        foreach ( $scenario ['labels'] as $label ) {
            $content .= " @" . $label;
        }
        $content .= "\n  Scenario";
        if ($scenario [$DESCRIPTION] != "") {
            $content .= " Outline";
        }
        $content .= ": " . $scenario ['summary'];
        // get test steps
        $ch = curl_init ();
        curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/zapi/latest/teststep/" . json_decode ( $response, true ) ['id'] );
        curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
        curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
        curl_setopt ( $ch, CURLOPT_USERPWD, "$username:$password" );
        curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
        curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
                $CONTENTTYPE 
        ) );
        $response = curl_exec ( $ch );
        curl_close ( $ch );
        // check for errors
        if (! is_array ( json_decode ( $response ) )) {
            header ( $ERROR );
            exit ();
        }
        $steps = json_decode ( $response, true );
        foreach ( $steps as $step ) {
            $content .= "\n    " . preg_replace('!\s+!', ' ', $step['step']);
        }
        $content .= "\n\n    " . implode ( "\n      ", explode ( "\n", $scenario [$DESCRIPTION] ) );
    }
    
    file_put_contents( $directory . $file, $content );
    $command = `zip -jr $zip $directory$file`;
}

if (file_exists ( $zip )) {
    header ( 'Content-Description: File Transfer' );
    header ( 'Content-Type: application/octet-stream' );
    header ( 'Content-Disposition: attachment; filename="' . basename ( $zip ) . '"' );
    header ( 'Expires: 0' );
    header ( 'Cache-Control: must-revalidate' );
    header ( 'Pragma: public' );
    header ( 'Content-Length: ' . filesize ( $zip ) );
    readfile ( $zip );
    exit ();
}
?>
