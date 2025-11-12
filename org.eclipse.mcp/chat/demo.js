/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

function demo() {
	setStyle(`13px`, `rgb(238, 238, 238)`, `rgb(52, 57, 61)`, `rgb(111, 197, 238)`, `rgb(138, 201, 242)`, `rgb(238, 238, 238)`, `rgb(81, 86, 88)`);
	
	const prompt = {"prompt":[{"description":"description","mimeType":"text/html","name":"sample.html","size":123456,"title":"sample.html","type":"resource_link","uri":"file:///no/where/dot/sample"},{"resource":{"mimeType":"text/html","text":"<html><body></body></html>","uri":"file:///no/where/dot/snippet.html"},"type":"resource"},{"text":"Using sample.html and the snippet of selected xml code, compare them to ","type":"text"},{"description":"description","mimeType":"text/xml","name":"mystery.xml","size":654321,"title":"mystery.xml","type":"resource_link","uri":"file:///no/where/dot/mystery"},{"text":"and see if the comparison matches ","type":"text"},{"resource":{"blob":"text/xml","mimeType":"<xml><body></body></xml>","uri":"file:///no/where/dot/snippet.xml"},"type":"resource"},{"text":". if not ask me for additional details","type":"text"}],"sessionId":"session1"};
	acceptPromptRequest(JSON.stringify(prompt));
	
	acceptSessionAgentThoughtChunk(JSON.stringify({"content":{"text":"**Im Thinking About**\n- one thing\n- another thing","type":"text"},"sessionUpdate":"session1"}.content));

	acceptSessionToolCall("tool1", "read: /eclipse/console/javaConsole", "read", "pending");
	acceptSessionToolCallUpdate("tool1", "completed");
	
	acceptSessionToolCall("tool3", "edit abc.txt", "edit", "pending");
	acceptSessionToolCall("tool4", "delete abc.txt", "delete", "pending");
	acceptSessionToolCall("tool5", "copy file to abc", "move", "pending");
	acceptSessionToolCall("tool6", "grep *abc", "search", "pending");
	acceptSessionToolCall("tool7", "ls -al", "execute", "pending");
	acceptSessionToolCallUpdate("tool7", "failed");	
	acceptSessionToolCall("tool8", "thinking of lorem ipsum", "think", "pending");
	acceptSessionToolCall("tool9", "fetch abc.txt", "fetch", "pending");
	acceptSessionToolCall("tool10", "switch mode: plan", "switch_mode", "pending");
	acceptSessionToolCall("tool11", "lorem ipsum", "other", "pending");
	
		
	acceptSessionAgentThoughtChunk(JSON.stringify({"content":{"text":"**Im Also Thinking About**\n- one thing\n- another thing","type":"text"},"sessionUpdate":"session1"}.content));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"Here i's what i came up with:\n```json\n \"a\": {\n\"B\": \"C\"","type":"text"},"sessionUpdate":"session1"}.content));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"\n}}\n```\nAnything else?","type":"text"},"sessionUpdate":"session1"}.content));
	
	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"\nUsing sample.html and the snippet of selected xml code, compare them to ","type":"text"},"sessionUpdate":"session1"}.content));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"description":"description","mimeType":"text/xml","name":"mystery.xml","size":654321,"title":"mystery.xml","type":"resource_link","uri":"file:///no/where/dot/mystery"},"sessionUpdate":"session1"}.content));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":"and see if the comparison matches ","type":"text"},"sessionUpdate":"session1"}.content));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"resource":{"blob":"text/xml","mimeType":"<xml><body></body></xml>","uri":"file:///no/where/dot/snippet.xml"},"type":"resource"},"sessionUpdate":"session1"}.content));

	acceptSessionAgentMessageChunk(JSON.stringify({"content":{"text":". if not ask me for additional details","type":"text"},"sessionUpdate":"session1"}.content));
}

if (getProgramIcon == null) { 
	function getProgramIcon(uri) {
		return icons[Math.floor(Math.random() * 5)];
	}
	icons = [
		"data:image/jpg;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAACbklEQVR4nM2S20/ScRjGveiyy/6ELrroov+gi24awe8r/MAUbdhmqZkt8IAhgkoOTQ1IYimSM+UwUqaCaKLiCQ+geIikRh5bkGvS2jxkmE/Mq5hW665ney8/n/fd3icp6b8M2+K9SCzT9anm8WGBaSLEM7jeU7q+WlZ99/k/gpfd7jPEMlNR7V7cXd7cOFpdXcLsnAsDI1bUGg2xNLV1m1Rbsn8rIKZps21m7hA/DhA73MP+wRcEQ7Nwj3eg1aZDu0uCDKU2SiQt9AmYZfYRkd37wRUI4uD7zrFgLy5Y/xTE0lo/TEPNKGmsR+YjNUiJPsAUNZ5LENBW36huMRwbXNvC7rftOBxFdGcTK1tT8Cwb0aCXIf2lH+ymSeSUCvfZ+SpxgoBv9YY18xH0vQ7g89cQwtEAQpExzLyzQuvoRK5jAVyLH8mNHtxX3jji5immEgQpxql5mWcDDfMf4VnoxtxKBxyTrdB36ZDnXkdqdwB0mw9EO4bCmjSk5JRFEl+nH5jNcgRQMLoGpd2Jtt4KPDZWokxdgFuG9uPtbP0kSO0riKtocDOl4cQP6Pq7OG3e2PXORfB73kBgHoRYdQ/yZxmQGQhuP30ISj0CpsKJbOGdGDejxJAoUPfQySq7n/N8On6qF3S7D2n6PpS3XIWs6QqkagZEiptgFNvA4pcHmZwHF072oNpio2p6VyjNKMiT+GiGIVJmQqq6hlIlG3eL8sAU1C0QulhxapF4cutZIm0ZYkle+Bnl9hhD7gRP0gxJFQe5wtxDwpe/JVxxwanwr6FE2nQqXzNBZddFkrOq1vlZRVGKLzURXuGlv8L/mp/1jYWohmUGtAAAAABJRU5ErkJggg==",
		"data:image/jpg;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABQUlEQVR4nK2My0oCYRiGvaCIyhw72KZF0KZuYS6gVUiI1Fh0cBolWrSICAwMppSSDosgqBYFodEFlJRIICqG1ZDOKLz1/zAyP3xmRB88m5fne1yu/7xjrQftOF3zGCeR7q4fA6nVXphWk+QuIeMoIpmHqtvXNpAM96FmNkmylwoy+zIONK+VaBfRl90wak2BiTkdspbC07WKxwsF6e/IXlhqkIH4Yj/ePxsC0tQWx7kxjwzEFjyoGg0BO+DcmEcGtkMSKh+WgB1wbswjA5uKF+U3q8Vz0eDPo/4dYWceGdiYHUCxarVIP5R4YDKkCzvzyMB6cBCFVxNnmRyiyVuMB3d5YEW/4bsN88hANDCEl4qJ8/s8fxwLxBGMXSFbMPhuwzwyoM4MI1+ud4R5ZGDJ70OuVO8I88jA/PQIfgsZ+Mt9AUor2KTAOoMGAAAAAElFTkSuQmCC",
		"data:image/jpg;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABU0lEQVR4nM2Q3y9CYRzG+5e4M2Y2Nqyss7U0YbH5B7CRi6g7rawVakyNplUSZeYiaXHBhZvEWnRcdCG/diyNytIee9+2s0VxXPHdPlff5/m8P0SifzXxgObizD8Fwvm2GongBNi9yaRgASm+5+9RLjzypHbH8W3JNG2AWafHom4WOdaMV9aEIjuHUsoA7kSLy51Ruqug57HMGCpio8ZIlyUuCjwf/QjJkTw5mL+FemweeAoLhuSrntGrcgAPQcGQfJVA2u8EMm7K0Ij9V1BBl8IFpFco8YgdCtUavIkigulyXZYiNzRHBW0yD3BtRixkhXzYDb2XhSuWryuxHWTADHoKPcp1CRU0SX049FsgHvBCu3oFx3EOztOXmpKF8C06lRtv7TK3hP+Djj5XtpnZhNqWhHWfw3I0W1NiCd2hVe4vtjA+iejzNIgDEEJj99bX8p/OB3MzycOix9H5AAAAAElFTkSuQmCC",
		"data:image/jpg;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAACZElEQVR4nKXRW0iTYRzHcb0IaouIbis6UNFFN5Ji1ihSE8HAOZKJERaEEdZFM2qiG9t0burrUmxZltvwsM2hzkMb2mym2FxYeRiKh1We1qzZNnqnVpq/3GpQ8bab/vC9eZ6HDw/PExb22xhk0U5j2XEXVYY7MQttxNHDYaHmcfkpn3dxFlT117HRRkQvN4pDIK1lceTK1zVQNWXmw1KfDj1xwvdPpLk0nvStrIGqt32SDYQHSw0bTcTJRUpAR8STn5dXMWT3gK8YhrjOhol5Ev61YB+cc9AWn/ZQApriM6TXt4obFQO4UvIcl4v6IFSNwL8W7L1jDmppLDVQK00g3eQ3pPK7UaB8Bf7DAVwUtsJylwbrvW3w7807ZlFTGEcNqCQJJCv3KZg5JsibbCDUQ7ggMMAzeA3eoeu/oO1QiP8C2ALzXLqo912GqH1dpHyJzKJeqDsnUdUyhjSeEfjUEshWfwgLPUz0ysJhqdi06i8AZIg6XFdLepApNaPp2Rso2sdgtExD82QSKVwDvpPWQDZdJHx2Av1yOtxWNtwv0hAAErI0TuZNvYuVrV6/rx/duP4IOqwz0JqmkMTRY93TFcjWEAHPqAC13HBoeHQ0i3f8BHYmVu3ak1y5Nz8ndSmZ2wV2Xicau+141DaKuCwt4JAH0glo0PK3oloYsWSUbmYYpVuO/fEWsrwkcvrjFxw8VwOVcRzlumFEnX+ABj4dWh4N4/YJvB7sA5GbSP0L+RwG/B1JrcSlAhNSbhsQk14KSXYkCjlRCO4X3op1UgLB2c9UsPalKF0HWMqZ3WerGSEP/+/8AGYz5JDc65+YAAAAAElFTkSuQmCC",
		"data:image/jpg;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAABnRSTlMAAAAAAABupgeRAAABLUlEQVR4nGNgIBtsmWCLC+HU8PffPwT6D2Vc2ZSJXc+BXsE/f/9B0G8Q+R/M+H9rb81VrHqAEr/+/IOhv3D2gxP9QKkrmzLW9Diga/jx6y9O9PsviiVXbv0Giv7HAb7++vvt598Fbc5Q1X0Lvmc1fmDwajAJegOUBjIgCCgO4X758efL99+zW1xg5ns1XLn//c2Hv0BtQBWOsW8/ffsNRJLWryAaIFwkJ3k1ACWA/vvw5RfQYRDjHePeQvT7Zb578eLt+y+oGoA2ADUYB76BaHj7+RcQIXPfff6J0AB0K9D1am6vfTPewf0AtAEoDtEAdBtQCj0qgHIPX/0AWvX8/Y+XH35AyDtPQVwIQtcA5MPlsKB3P5rLfVA0VBcH1JT4wZA/jIQjkDiWFEUQAAAJUX5bxk9gdgAAAABJRU5ErkJggg=="
	]
}

	