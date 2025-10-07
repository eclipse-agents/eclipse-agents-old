function demo() {
	setStyle(`13px`, `rgb(238, 238, 238)`, `rgb(52, 57, 61)`, `rgb(111, 197, 238)`, `rgb(138, 201, 242)`, `rgb(238, 238, 238)`, `rgb(81, 86, 88)`);
	addPromptTurn();

	addSessionPrompt("My question is asdf asdf asdf");
	addResourceLink("File1.txt", "", "fa-file");
	addResourceLink("folderName", "", "fa-folder");
	addAgentThoughtChunk(`**Im Thinking About**
- one thing
- another thing`);

	addAgentThoughtChunk(`**Im Also Thinking About**
- one thing
- another thing`);

	addAgentMessageChunk(`Here is what i came up with:
\`\`\`json
{ "a": {
	"B": "C"`
);
	addAgentMessageChunk(`
}}
\`\`\`
Anything else?`);

	addPromptTurn();
		addSessionPrompt("My question is asdf asdf asdf");
	//addResourceLink("File1.txt", "", "resource_link", "fa-file");
	//addResourceLink("folderName", "", "resource_link", "fa-folder");
	addAgentThoughtChunk(`**Im Also Thinking About**
- one thing
- another thing`);

	addAgentThoughtChunk(`**Im Also Thinking About**
- one thing
- another thing`);

	addAgentMessageChunk(`Here is what i also came up with:
\`\`\`json
{ "a": {
	"B": "C"`
);
	addAgentMessageChunk(`
}}c
\`\`\`
Anything else?`);
}
