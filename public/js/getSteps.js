//our step functionality
function step() {
    this.string = arguments[0];
    this.inputs = [];
    for (var i = 1; i < arguments.length; i++) {
        this.inputs.push(arguments[i]);
    }
}
// our keypair functionality
function keypair(key, value) {
    this.key = key;
    this.value = value;
}
// our basic setup
var testSteps = {};
testSteps.whens = [];
testSteps.thens = [];