<?php
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
?>