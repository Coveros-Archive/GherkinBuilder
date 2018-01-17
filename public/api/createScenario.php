<?php
$params = parse_ini_file ( dirname ( __DIR__ ) . DIRECTORY_SEPARATOR . "props.ini" );

$ERROR = "HTTP/1.1 500 Internal Server Error";
$CONTENTTYPE = "Content-Type: application/json";
$TITLECHECK = "/<title>(.*)<\/title>/";
$PROJECT = "project";
$FEATURE = "feature";
$SCENARIOTAGS = "scenarioTags";
$SCENARIOLINKS = "scenarioLinks";
$SCENARIOTITLE = "scenarioTitle";
$SCENARIODESCRIPTION = "scenarioDescription";
$BACKGROUNDSTEPS = "backgroundSteps";
$SCENARIOTESTSTEPS = "scenarioTestSteps";
$SCENARIOEXAMPLES = "scenarioExamples";
$TAGS = "tags";
$DATA = "data";
$INPUTS = "inputs";

$data = new \stdClass ();
$data->fields = new \stdClass ();
$data->fields->issuetype = new \stdClass ();
$data->fields->issuetype->name = "Test";

if (! isset ( $_POST ['auth'] ) || $_POST ['auth'] == "Og==") {
    echo "Authorization not provided";
    header ( $ERROR );
    exit ();
} else {
    $auth = base64_decode ( $_POST ['auth'] );
}

if (! isset ( $_POST [$PROJECT] ) || $_POST [$PROJECT] == "") {
    echo "Project not provided";
    header ( $ERROR );
    exit ();
} else {
    $data->fields->project = new \stdClass ();
    $data->fields->project->key = $_POST [$PROJECT];
}

if (! isset ( $_POST [$FEATURE] ) || $_POST [$FEATURE] == "") {
    echo "Feature not provided";
    header ( $ERROR );
    exit ();
} else {
    $data->fields->{$params ['epic_link_field']} = $_POST [$FEATURE];
}

if (isset ( $_POST [$SCENARIOTAGS] ) && ! empty ( $_POST [$SCENARIOTAGS] )) {
    $tags = array ();
    foreach ( $_POST [$SCENARIOTAGS] as $tag ) {
        array_push ( $tags, substr ( $tag, 1 ) );
    }
    $data->fields->labels = $tags;
}

if (! isset ( $_POST [$SCENARIOTITLE] ) || $_POST [$SCENARIOTITLE] == "") {
    echo "Scenario title not provided";
    header ( $ERROR );
    exit ();
} else {
    $data->fields->summary = $_POST [$SCENARIOTITLE];
}

if (isset ( $_POST [$SCENARIOEXAMPLES] ) && ! empty ( $_POST [$SCENARIOEXAMPLES] )) {
    $exampleString = "";
    foreach ( $_POST [$SCENARIOEXAMPLES] as $example ) {
        if (isset ( $example [$TAGS] ) && ! empty ( $example [$TAGS] )) {
            $tags = array ();
            foreach ( $example [$TAGS] as $tag ) {
                array_push ( $tags, substr ( $tag, 1 ) );
            }
            $exampleString .= implode ( " ", $tags ) . "\n";
        }
        if (isset ( $example [$INPUTS] ) && ! empty ( $example [$INPUTS] )) {
            $exampleString .= "Examples:\n";
            $exampleString .= "| " . implode ( " | ", $example [$INPUTS] ) . " |\n";
        } else {
            echo json_encode ( "Something went wrong with your example" );
            header ( $ERROR );
            exit ();
        }
        if (isset ( $example [$DATA] ) && ! empty ( $example [$DATA] )) {
            foreach ( $example [$DATA] as $row ) {
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
        $CONTENTTYPE 
) );
$response = curl_exec ( $ch );
curl_close ( $ch );

if (! is_object ( json_decode ( $response ) )) {
    preg_match ( $TITLECHECK, $response, $match );
    echo $match [1];
    header ( $ERROR );
    exit ();
}

// add links if they are passed
if (isset ( $_POST [$SCENARIOLINKS] ) && ! empty ( $_POST [$SCENARIOLINKS] )) {
    $key = json_decode ( $response, true ) ['key'];
    foreach ( $_POST [$SCENARIOLINKS] as $link ) {
        $links = new \stdClass ();
        $links->type = new \stdClass ();
        $links->type->name = "Test";
        $links->inwardIssue = new \stdClass ();
        $links->inwardIssue->key = $key;
        $links->outwardIssue = new \stdClass ();
        $links->outwardIssue->key = $link;
        $ch = curl_init ();
        curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/issueLink" );
        curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
        curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
        curl_setopt ( $ch, CURLOPT_POST, TRUE );
        curl_setopt ( $ch, CURLOPT_POSTFIELDS, json_encode ( $links ) );
        curl_setopt ( $ch, CURLOPT_USERPWD, $auth );
        curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
        curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
                $CONTENTTYPE
        ) );
        $response = curl_exec ( $ch );
        curl_close ( $ch );
    }
}

$scenarioId = json_decode ( $response, true ) ['id'];

// add the description to the comments
if (isset ( $_POST [$SCENARIODESCRIPTION] ) && $_POST [$SCENARIODESCRIPTION] != "") {
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/api/2/issue/" . $scenarioId . "/comment" );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
    curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
    curl_setopt ( $ch, CURLOPT_POST, TRUE );
    curl_setopt ( $ch, CURLOPT_POSTFIELDS, "{\"body\":\"" . $_POST [$SCENARIODESCRIPTION] . "\"}" );
    curl_setopt ( $ch, CURLOPT_USERPWD, $auth );
    curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
            $CONTENTTYPE 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    
    if (! is_object ( json_decode ( $response ) )) {
        preg_match ( $TITLECHECK, $response, $match );
        echo $match [1];
        header ( $ERROR );
        exit ();
    }
}
// setup test steps
$testSteps = array ();
if (isset ( $_POST [$BACKGROUNDSTEPS] ) && ! empty ( $_POST [$BACKGROUNDSTEPS] )) {
    $testSteps = array_merge ( $testSteps, $_POST [$BACKGROUNDSTEPS] );
}
if (isset ( $_POST [$SCENARIOTESTSTEPS] ) && ! empty ( $_POST [$SCENARIOTESTSTEPS] )) {
    $testSteps = array_merge ( $testSteps, $_POST [$SCENARIOTESTSTEPS] );
}
foreach ( $testSteps as $testStep ) {
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $params ['base'] . "/rest/zapi/latest/teststep/" . $scenarioId );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
    curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
    curl_setopt ( $ch, CURLOPT_POST, TRUE );
    curl_setopt ( $ch, CURLOPT_POSTFIELDS, "{\"step\":\"" . addcslashes ( $testStep, '"' ) . "\"}" );
    curl_setopt ( $ch, CURLOPT_USERPWD, $auth );
    curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
            $CONTENTTYPE 
    ) );
    $response = curl_exec ( $ch );
    curl_close ( $ch );
    
    if (! is_object ( json_decode ( $response ) )) {
        preg_match ( $TITLECHECK, $response, $match );
        if (sizeof ( $match ) > 1) {
            echo $match [1];
        } else {
            echo $response;
        }
        header ( $ERROR );
        exit ();
    }
}

?>