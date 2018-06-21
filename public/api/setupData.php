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