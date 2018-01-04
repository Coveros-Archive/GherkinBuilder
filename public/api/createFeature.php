<?php
$params = parse_ini_file ( dirname ( __DIR__ ) . DIRECTORY_SEPARATOR . "props.ini" );

$data->fields->issuetype->name = "Epic";

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

if (isset ( $_POST ['featureTags'] ) && ! empty ( $_POST ['featureTags'] )) {
    $tags = array();
    foreach( $_POST ['featureTags'] as $tag ) {
        array_push( $tags, substr($tag, 1));
    }
    $data->fields->labels = $tags;
}

if (! isset ( $_POST ['featureTitle'] ) || $_POST ['featureTitle'] == "") {
    echo json_encode ( "Feature title not provided" );
    header ( "HTTP/1.1 500 Internal Server Error" );
    exit ();
} else {
    $data->fields->summary = $_POST ['featureTitle'];
    $data->fields->{$params ['epic_name_field']} = $_POST ['featureTitle'];
}

if (isset ( $_POST ['featureDescription'] ) && $_POST ['featureDescription'] != "") {
    $data->fields->description = $_POST ['featureDescription'];
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

echo $response;

?>