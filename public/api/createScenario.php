<?php
$params = parse_ini_file ( dirname ( __DIR__ ) . DIRECTORY_SEPARATOR . "props.ini" );

$data->fields->issuetype->name = "Test";

if (! isset ( $_POST ['auth'] ) || $_POST ['auth'] == "Og==") {
    echo json_encode ( "Authorization not provided" );
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
} else {
    $auth = base64_decode ( $_POST ['auth'] );
}

if (! isset ( $_POST ['project'] ) || $_POST ['project'] == "") {
    echo json_encode ( "Project not provided" );
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
} else {
    $data->fields->project->key = $_POST ['project'];
}

if (! isset ( $_POST ['feature'] ) || $_POST ['feature'] == "") {
    echo json_encode ( "Feature not provided" );
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
} else {
    $data->fields->{$params ['epic_link_field']} = $_POST ['feature'];
}

if (isset ( $_POST ['scenarioTags'] ) && ! empty ( $_POST ['scenarioTags'] )) {
    $tags = array ();
    foreach ( $_POST ['scenarioTags'] as $tag ) {
        array_push ( $tags, substr ( $tag, 1 ) );
    }
    $data->fields->labels = $tags;
}

if (! isset ( $_POST ['scenarioTitle'] ) || $_POST ['scenarioTitle'] == "") {
    echo json_encode ( "Scenario title not provided" );
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
} else {
    $data->fields->summary = $_POST ['scenarioTitle'];
}

if (isset ( $_POST ['scenarioExamples'] ) && ! empty ( $_POST ['scenarioExamples'] )) {
    $exampleString = "";
    foreach ( $_POST ['scenarioExamples'] as $example ) {
        if (isset ( $example ['tags'] ) && ! empty ( $example ['tags'] )) {
            $tags = array ();
            foreach ( $example ['tags'] as $tag ) {
                array_push ( $tags, substr ( $tag, 1 ) );
            }
            $exampleString .= implode ( " ", $tags ) . "\n";
        }
        if (isset ( $example ['inputs'] ) && ! empty ( $example ['inputs'] )) {
            $exampleString .= "Examples:\n";
            $exampleString .= "| " . implode ( " | ", $example ['inputs'] ) . " |\n";
        } else {
            echo json_encode ( "Something went wrong with your example" );
            header ( "HTTP/1.1 500 Internal Server Error" );
            exit ();
        }
        if (isset ( $example ['data'] ) && ! empty ( $example ['data'] )) {
            foreach ( $example ['data'] as $row ) {
                $exampleString .= "| " . implode ( " | ", array_values ( $row ) ) . " |\n";
            }
        }
    }
    $data->fields->description = $exampleString;
}

// make curl command
$ch = curl_init ();
curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/issue" );
curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
curl_setopt ( $ch, CURLOPT_POST, TRUE );
curl_setopt ( $ch, CURLOPT_POSTFIELDS, json_encode ( $data ) );
curl_setopt ( $ch, CURLOPT_USERPWD, $auth );
curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
        "Content-Type: application/json" 
) );
$response = curl_exec ( $ch );
curl_close ( $ch );

if (! is_object ( json_decode ( $response ) )) {
    preg_match ( '/<title>(.*)<\/title>/', $response, $match );
    echo json_encode ( $match [1] );
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
}

$scenarioId = json_decode ( $response, true ) ['id'];

// add the description to the comments
if (isset ( $_POST ['scenarioDescription'] ) && $_POST ['scenarioDescription'] != "") {
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/issue/" . $scenarioId . "/comment" );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
    curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
    curl_setopt ( $ch, CURLOPT_POST, TRUE );
    curl_setopt ( $ch, CURLOPT_POSTFIELDS, "{\"body\":\"" . $_POST ['scenarioDescription'] . "\"}" );
    curl_setopt ( $ch, CURLOPT_USERPWD, $auth );
    curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
            "Content-Type: application/json" 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    
    if (! is_object ( json_decode ( $response ) )) {
        preg_match ( '/<title>(.*)<\/title>/', $response, $match );
        echo json_encode ( $match [1] );
        header ( "HTTP/1.1 500 Internal Server Error" );
        exit ();
    }
}
// setup test steps
$testSteps = array ();
if (isset ( $_POST ['backgroundSteps'] ) && ! empty ( $_POST ['backgroundSteps'] )) {
    $testSteps = array_merge ( $testSteps, $_POST ['backgroundSteps'] );
}
if (isset ( $_POST ['scenarioTestSteps'] ) && ! empty ( $_POST ['scenarioTestSteps'] )) {
    $testSteps = array_merge ( $testSteps, $_POST ['scenarioTestSteps'] );
}
foreach ( $testSteps as $testStep ) {
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/zapi/latest/teststep/" . $scenarioId );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
    curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
    curl_setopt ( $ch, CURLOPT_POST, TRUE );
    curl_setopt ( $ch, CURLOPT_POSTFIELDS, "{\"step\":\"" . addslashes ( $testStep ) . "\"}" );
    curl_setopt ( $ch, CURLOPT_USERPWD, $auth );
    curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
            "Content-Type: application/json" 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    
    if (! is_object ( json_decode ( $response ) )) {
        preg_match ( '/<title>(.*)<\/title>/', $response, $match );
        echo json_encode ( $match [1] );
        header ( "HTTP/1.1 500 Internal Server Error" );
        exit ();
    }
}

?>