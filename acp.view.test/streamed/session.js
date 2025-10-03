// Configure marked to use Prism for syntax highlighting
marked.setOptions({
    highlight: function (code, lang) {
        if (Prism.languages[lang]) {
            return Prism.highlight(
                code,
                Prism.languages[lang],
                lang,
            );
        } else {
            return code;
        }
    },
});

const thought1 = `**Initiating COBOL Elaboration**\n\nI\u0027m homing in on the user\u0027s need for a COBOL \"Hello, World!\" explanation. Right now, I\u0027m drilling down on the core elements: identifying the necessary divisions (IDENTIFICATION, ENVIRONMENT, DATA, PROCEDURE), the expected verbosity, and the structured nature inherent to the language.`;
const thought2 = `**Outlining the Program\u0027s Anatomy**\n\nI\u0027ve solidified the plan to break down the COBOL \"Hello, World!\" program into manageable pieces. I will start with an overview of the divisions, then display the code and explain each part in detail. I\u0027m working on the specific verbiage for explaining the \`DISPLAY\` verb, and the overall structure of the program.`
const thought3 = `**Structuring Code Presentation**\n\nI\u0027m now focusing on presenting the COBOL code snippet itself. I decided the most straightforward approach is to forgo a \`DATA DIVISION\`, as it adds unnecessary complexity to a \"Hello, World!\" example. I\u0027ll carefully format the code, highlighting essential elements.`
const thought4 = `**Formulating Concise Explanations**\n\nI\u0027ve wrapped up the core program explanation. The focus now is refining the language, ensuring clarity, and considering the target audience\u0027s potential lack of COBOL familiarity. I\u0027m streamlining the breakdown, aiming for brevity and precision in the code descriptions.`
const thought5 = `**Synthesizing the Final Response**\n\nI\u0027m fine-tuning the final presentation now. I\u0027m ensuring the explanation flows logically, from general COBOL concepts to the \"Hello, World!\" code. I\u0027ll emphasize key terms and structure the explanation for maximum clarity. Finally, I\u0027ll create a closing statement.`
const agentMessage1 = `Of course. A \"Hello, World!\" program in COBOL is a great way to see the language\u0027s highly structured nature.\n\nCOBOL programs are organized into four main parts called \`DIVISION\`s. For a simple program, you only need two:\n\n1.  \`IDENTIFICATION DIVISION\`: Provides metadata about the`;
const agentMessage2 = `, like its name.\n2.  \`PROCEDURE DIVISION\`: Contains the actual instructions the program will execute.\n\nHere is a minimal \"Hello, World!\" program in COBOL:\n\n\`\`\`cobol\nIDENTIFICATION DIVISION.\nPROGRAM-ID. HELLOWORLD.\n\nPROCEDURE DIVISION.`
const agentMessage3 = `\n    DISPLAY \u0027Hello, World!\u0027.\n    STOP RUN.\n\`\`\`\n\n### Breakdown:\n\n*   **\`IDENTIFICATION DIVISION.\`**: This is the header section. It\u0027s mandatory.\n*   **\`PROGRAM-ID. HELLOWORLD.\`**: This line within the \`IDENTIFICATION DIVISION\` gives`;
const agentMessage4 = ` the program a name, in this case, \`HELLOWORLD\`.\n*   **\`PROCEDURE DIVISION.\`**: This is where the program\u0027s logic resides.\n*   **\`DISPLAY \u0027Hello, World!\u0027\`**: This is the core instruction. The \`DISPLAY\` verb tells the system to print the following`;
const agentMessage5 = ` literal string to the standard output (e.g., the console).\n*   **\`STOP RUN.\`**: This statement halts the program\u0027s execution and returns control to the operating system.`;

// Convert markdown to HTML and insert into the page with delays
async function updateElements() {
    const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
    
    document.getElementById("thought1").innerHTML = marked.parse(thought1);
    await sleep(1000);
    
    document.getElementById("thought2").innerHTML = marked.parse(thought2);
    await sleep(1000);
    
    document.getElementById("thought3").innerHTML = marked.parse(thought3);
    await sleep(1000);
    
    document.getElementById("thought4").innerHTML = marked.parse(thought4);
    await sleep(1000);
    
    document.getElementById("thought5").innerHTML = marked.parse(thought5);
    await sleep(1000);
    
    document.getElementById("agent-message1").innerHTML = marked.parse(
        agentMessage1
    );

    await sleep(1000);
     document.getElementById("agent-message1").innerHTML = marked.parse(
        agentMessage1 +
        agentMessage2 
    );

    await sleep(1000);
     document.getElementById("agent-message1").innerHTML = marked.parse(
        agentMessage1 +
        agentMessage2 +
        agentMessage3 
    );

    await sleep(1000);
     document.getElementById("agent-message1").innerHTML = marked.parse(
        agentMessage1 +
        agentMessage2 +
        agentMessage3 +
        agentMessage4 
    );

    await sleep(1000);
    
     document.getElementById("agent-message1").innerHTML = marked.parse(
        agentMessage1 +
        agentMessage2 +
        agentMessage3 +
        agentMessage4 +
        agentMessage5
    );

    // Re-run Prism highlighting
    Prism.highlightAll();
}

updateElements();
