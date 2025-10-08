function demo() {
	setStyle(`13px`, `rgb(238, 238, 238)`, `rgb(52, 57, 61)`, `rgb(111, 197, 238)`, `rgb(138, 201, 242)`, `rgb(238, 238, 238)`, `rgb(81, 86, 88)`);
	
	const prompt = {"prompt":[{"description":"description","mimeType":"text/html","name":"sample.html","size":123456,"title":"sample.html","type":"resource_link","uri":"file:///no/where/dot/sample"},{"resource":{"mimeType":"text/html","text":"<html><body></body></html>","uri":"file:///no/where/dot/snippet.html"},"type":"resource"},{"text":"Using sample.html and the snippet of selected xml code, compare them to ","type":"text"},{"description":"description","mimeType":"text/xml","name":"mystery.xml","size":654321,"title":"mystery.xml","type":"resource_link","uri":"file:///no/where/dot/mystery"},{"text":"and see if the comparison matches ","type":"text"},{"resource":{"blob":"text/xml","mimeType":"<xml><body></body></xml>","uri":"file:///no/where/dot/snippet.xml"},"type":"resource"},{"text":". if not ask me for additional details","type":"text"}],"sessionId":"session1"};
	acceptPromptRequest(JSON.stringify(prompt));
	
	acceptSessionAgentThoughtChunk(JSON.stringify({"content":{"text":"**Im Thinking About**\n- one thing\n- another thing","type":"text"},"sessionUpdate":"session1"}));

	acceptSessionAgentThoughtChunk(JSON.stringify({"content":{"text":"**Im Also Thinking About**\n- one thing\n- another thing","type":"text"},"sessionUpdate":"session1"}));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"Here is what i came up with:\n```json\n \"a\": {\n\"B\": \"C\"","type":"text"},"sessionUpdate":"session1"}));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"\n}}\n```\nAnything else?","type":"text"},"sessionUpdate":"session1"}));
	
	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"\nUsing sample.html and the snippet of selected xml code, compare them to ","type":"text"},"sessionUpdate":"session1"}));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"description":"description","mimeType":"text/xml","name":"mystery.xml","size":654321,"title":"mystery.xml","type":"resource_link","uri":"file:///no/where/dot/mystery"},"sessionUpdate":"session1"}));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"and see if the comparison matches ","type":"text"},"sessionUpdate":"session1"}));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"resource":{"blob":"text/xml","mimeType":"<xml><body></body></xml>","uri":"file:///no/where/dot/snippet.xml"},"type":"resource"},"sessionUpdate":"session1"}));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":". if not ask me for additional details","type":"text"},"sessionUpdate":"session1"}));
}
