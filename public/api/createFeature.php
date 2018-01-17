<?php
$params = parse_ini_file ( dirname ( __DIR__ ) . DIRECTORY_SEPARATOR . "props.ini" );

$ERROR = "HTTP/1.1 500 Internal Server Error";
$PROJECT = "project";
$FEATURETAGS = "featureTags";
$FEATURELINKS = "featureLinks";
$FEATURETITLE = "featureTitle";
$FEATUREDESCRIPTION = "featureDescription";

$data = new \stdClass ();
$data->fields = new \stdClass ();
$data->fields->issuetype = new \stdClass ();
$data->fields->issuetype->name = "Epic";

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

$tags = array (
        "Feature" 
);
if (isset ( $_POST [$FEATURETAGS] ) && ! empty ( $_POST [$FEATURETAGS] )) {
    foreach ( $_POST [$FEATURETAGS] as $tag ) {
        array_push ( $tags, substr ( $tag, 1 ) );
    }
}
$data->fields->labels = $tags;

if (! isset ( $_POST [$FEATURETITLE] ) || $_POST [$FEATURETITLE] == "") {
    echo "Feature title not provided";
    header ( $ERROR );
    exit ();
} else {
    $data->fields->summary = $_POST [$FEATURETITLE];
    $data->fields->{$params ['epic_name_field']} = "Test Suite: " . $_POST [$FEATURETITLE];
}

if (isset ( $_POST [$FEATUREDESCRIPTION] ) && $_POST [$FEATUREDESCRIPTION] != "") {
    $data->fields->description = $_POST [$FEATUREDESCRIPTION];
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

if (is_object ( json_decode ( $response ) )) {
    echo $response;
} else {
    preg_match ( '/<title>(.*)<\/title>/', $response, $match );
    echo $match [1];
    header ( $ERROR );
    exit ();
}

// add links if they are passed
if (isset ( $_POST [$FEATURELINKS] ) && ! empty ( $_POST [$FEATURELINKS] )) {
    $key = json_decode ( $response, true ) ['key'];
    foreach ( $_POST [$FEATURELINKS] as $link ) {
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
                "Content-Type: application/json" 
        ) );
        $response = curl_exec ( $ch );
        curl_close ( $ch );
    }
}

?>