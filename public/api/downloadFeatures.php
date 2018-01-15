<?php
$params = parse_ini_file ( dirname ( __DIR__ ) . DIRECTORY_SEPARATOR . "props.ini" );

// setup our directory to host our files
$directory = '../tmp/';
if (! file_exists ( $directory )) {
    mkdir ( $directory, 0777, true );
}

// get our jira creds
if (! isset ( $_POST ['username'] ) || $_POST ['username'] == "") {
    echo "Authorization not provided";
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
} else {
    $username = $_POST ['username'];
}
if (! isset ( $_POST ['password'] ) || $_POST ['password'] == "") {
    echo "Authorization not provided";
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
} else {
    $password = $_POST ['password'];
}

// setup our zip files
$zip = new ZipArchive ();
$filename = $directory . "Features-" . time () . ".zip";
if ($zip->open ( $filename, ZipArchive::CREATE ) !== TRUE) {
    exit ( "cannot open <$filename>\n" );
}

// get all of the feature files
$ch = curl_init ();
curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/search?jql=project=" . $params ['project'] . "%20AND%20labels=Feature&fields=key&maxResults=9999" );
curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
curl_setopt ( $ch, CURLOPT_USERPWD, "$username:$password" );
curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
        "Content-Type: application/json" 
) );
$response = curl_exec ( $ch );
curl_close ( $ch );
// check for errors
if (! is_object ( json_decode ( $response ) )) {
    header ( "HTTP/1.1 500 Internal Server Error" );
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
            "Content-Type: application/json" 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    // check for errors
    if (! is_object ( json_decode ( $response ) )) {
        header ( "HTTP/1.1 500 Internal Server Error" );
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
    $content .= "\n  " . implode ( "\n  ", explode ( "\n", $feature ['description'] ) );
    // get all scenario details
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/search?jql=project=" . $params ['project'] . "%20AND%20\"Epic%20Link\"=" . $issue ['key'] . "&fields=key&maxResults=9999" );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
    curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
    curl_setopt ( $ch, CURLOPT_USERPWD, "$username:$password" );
    curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
            "Content-Type: application/json" 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    // check for errors
    if (! is_object ( json_decode ( $response ) )) {
        header ( "HTTP/1.1 500 Internal Server Error" );
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
                "Content-Type: application/json" 
        ) );
        $response = curl_exec ( $ch );
        curl_close ( $ch );
        // check for errors
        if (! is_object ( json_decode ( $response ) )) {
            header ( "HTTP/1.1 500 Internal Server Error" );
            exit ();
        }
        $scenario = json_decode ( $response, true ) ['fields'];
        // add scenario content
        $content .= "\n\n  @" . $isswe ['key'];
        foreach ( $scenario ['labels'] as $label ) {
            $content .= " @" . $label;
        }
        $content .= "\n  Scenario";
        if ($scenario ['description'] != "") {
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
                "Content-Type: application/json" 
        ) );
        $response = curl_exec ( $ch );
        curl_close ( $ch );
        // check for errors
        if (! is_array ( json_decode ( $response ) )) {
            header ( "HTTP/1.1 500 Internal Server Error" );
            exit ();
        }
        $steps = json_decode ( $response, true );
        foreach ( $steps as $step ) {
            $content .= "\n    " . preg_replace('!\s+!', ' ', $step['step']);
        }
        $content .= "\n\n    " . implode ( "\n      ", explode ( "\n", $scenario ['description'] ) );
    }
    
    $zip->addFromString ( $file, $content );
}

$zip->close ();

if (file_exists ( $filename )) {
    header ( 'Content-Description: File Transfer' );
    header ( 'Content-Type: application/octet-stream' );
    header ( 'Content-Disposition: attachment; filename="' . basename ( $filename ) . '"' );
    header ( 'Expires: 0' );
    header ( 'Cache-Control: must-revalidate' );
    header ( 'Pragma: public' );
    header ( 'Content-Length: ' . filesize ( $filename ) );
    readfile ( $filename );
    exit ();
}
?>