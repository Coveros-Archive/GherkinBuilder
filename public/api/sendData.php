<?php
$ERROR = "HTTP/1.1 500 Internal Server Error";
$AUTH = "auth";
$LINK = "link";
$FEATURE = "Feature";
$SCENARIOS = "Scenarios";

if (! isset ( $_POST [$AUTH] ) || $_POST [$AUTH] == "Og==") {
    echo "Authorization not provided";
    header ( $ERROR );
    exit ();
} else {
    $auth = base64_decode ( $_POST [$AUTH] );
}

if (! isset ( $_POST [$LINK] ) || $_POST [$LINK] == "") {
    echo "Link not provided";
    header ( $ERROR );
    exit ();
} else {
    $link = $_POST [$LINK];
}

$feature = "";
if (isset ( $_POST [$FEATURE] )) {
    $feature = "Feature=" . urlencode( json_encode( $_POST [$FEATURE] ) );
}

if (! isset ( $_POST [$SCENARIOS] ) || $_POST [$SCENARIOS] == "") {
    echo "Scenarios not provided";
    header ( $ERROR );
    exit ();
} else {
    $scenarios = "Scenarios=" . urlencode( json_encode( $_POST [$SCENARIOS] ) );
}

$fullLink = $link . "&" . $feature . "&" . $scenarios;

// make curl command
$ch = curl_init ();
curl_setopt ( $ch, CURLOPT_URL, $fullLink);
curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, TRUE );
curl_setopt ( $ch, CURLOPT_HEADER, FALSE );
curl_setopt ( $ch, CURLOPT_POST, TRUE );
curl_setopt ( $ch, CURLOPT_USERPWD, $auth );
curl_setopt ( $ch, CURLOPT_SSL_VERIFYHOST, false );
curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
        "Content-Type: application/json" 
) );
$response = curl_exec ( $ch );
curl_close ( $ch );

if ($response == "") {
    echo $fullLink;
} else {
    preg_match ( '/<title>(.*)<\/title>/', $response, $match );
    echo $match [1];
    header ( $ERROR );
    exit ();
}
?>