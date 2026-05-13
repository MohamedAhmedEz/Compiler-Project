import { useState, useRef } from 'react'
import './App.css'

// Updated Test Cases
const TEST_CASES = [
  // 1. Valid - Easy
  "x := 5;\nprint x;", 
  
  // 2. Valid - Complex Arithmetic
  "y := 10 + 5 * 2 - 8 / 4;\nprint y;", 
  
  // 3. Valid - Associativity
  "a := 10 - 5 - 2;\nprint a;", 
  
  // 4. Valid - Nested Paren
  "b := ((10 + 2) * 3) / (4 - 2);\nprint b;", 
  
  // 5. Valid - Unary Exp
  "c := -5 + 10;\nprint c;", 
  
  // 6. Valid - Comments Handling
  "// Initial value\nx := 10; /* Assigning 10 to x */\nprint x;", 
  
  // 7. Invalid Lexer - Unknown Character
  "x := 10 @ 5;\nprint x;", 
  
  // 8. Invalid Parser - Missing Semi
  "x := 10\nprint x;", 
  
  // 9. Invalid Parser - Missing )
  "x := (10 + 5 * 2;\nprint x;", 

  // 10. Invalid Parser - 4 statements, 3rd has parser error (unexpected '*')
  "v1 := 10;\nv2 := 20;\nv3 := v1 + * v2;\nprint v3;",
  
  // 11. Invalid Semantic - Used before initialized
  "print z;" 
];

function App() {
  const [code, setCode] = useState(TEST_CASES[0]);
  const [messages, setMessages] = useState(["Ready to compile"]);
  const [tokens, setTokens] = useState([]);
  const [isSuccess, setIsSuccess] = useState(true);
  const [zoom, setZoom] = useState(1);
  
  const graphRef = useRef(null);

  const handleCompile = async () => {
    setMessages(["Compiling..."]);
    
    try {
      const response = await fetch("http://localhost:8080/api/compile", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sourceCode: code })
      });
      
      const data = await response.json();
      
      setIsSuccess(data.success);
      setMessages(data.messages || ["Unknown Error"]);
      setTokens(data.tokens || []);
      
      // We now draw the graph even if success is false, as long as astDot exists!
      if (data.astDot && window.Viz) {
        const viz = new window.Viz();
        viz.renderSVGElement(data.astDot)
          .then(svg => {
            svg.style.width = "100%";
            svg.style.height = "100%";
            graphRef.current.innerHTML = ""; 
            graphRef.current.appendChild(svg);
            setZoom(1); 
          })
          .catch(error => console.error("Graph rendering error:", error));
      } else {
         if (graphRef.current) graphRef.current.innerHTML = ""; 
      }
      
    } catch (err) {
      setIsSuccess(false);
      setMessages(["🚨 Cannot connect to Java API on port 8080"]);
    }
  };

  const downloadSvg = () => {
    const svg = graphRef.current.querySelector('svg');
    if (!svg) return alert("No graph to download!");
    const source = new XMLSerializer().serializeToString(svg);
    const blob = new Blob([source], {type: "image/svg+xml;charset=utf-8"});
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "tinycalc-ast.svg";
    link.click();
  };

  return (
    <div className="app-container">
      <header className="top-bar">
        <div className="logo">⚙️ TinyCalc IDE</div>
        
        {/* Unnamed Test Case Buttons */}
        <div className="test-cases-buttons" style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
          {TEST_CASES.map((testCode, index) => (
            <button 
              key={index}
              onClick={() => setCode(testCode)}
              className="test-btn"
            >
              Test {index + 1}
            </button>
          ))}
        </div>

        <button onClick={handleCompile} className="compile-btn">
          ▶ Run Compiler
        </button>
      </header>

      {/* Multi-message display banner */}
      <div className={`status-banner ${isSuccess ? 'success' : 'error'}`}>
        {messages.map((msg, i) => (
          <div key={i}>{isSuccess ? '✅' : '❌'} {msg}</div>
        ))}
      </div>

      <div className="workspace">
        <div className="panel editor-panel">
          <div className="panel-header">source.tc</div>
          <textarea 
            className="code-editor"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            spellCheck="false"
          />
        </div>

        <div className="panel tokens-panel">
          <div className="panel-header">Token Stream ({tokens.length})</div>
          <div className="token-list">
            {tokens.map((t, i) => (
              <div key={i} className="token-item">
                <span className={`token-type ${t.type.toLowerCase()}`}>
                  {t.type}
                </span>
                <span className="token-lexeme">{t.lexeme}</span>
              </div>
            ))}
            {tokens.length === 0 && <div className="empty-text">No tokens generated.</div>}
          </div>
        </div>

       <div className="panel ast-panel">
          <div className="panel-header ast-header">
            <span>Abstract Syntax Tree {(!isSuccess && graphRef.current?.innerHTML) ? "(Partial)" : ""}</span>
            <div className="toolbar">
              <button onClick={() => setZoom(z => z - 0.2)}>➖</button>
              <span className="zoom-level">{Math.round(zoom * 100)}%</span>
              <button onClick={() => setZoom(z => z + 0.2)}>➕</button>
              <button onClick={downloadSvg} className="dl-btn">💾 Export SVG</button>
            </div>
          </div>
          
          <div className="graph-viewport">
            {(!isSuccess && !graphRef.current?.innerHTML) && (
               <p className="empty-text" style={{position: 'absolute', width: '100%'}}>
                 No valid AST generated.
               </p>
            )}
            
            <div 
              className="graph-canvas" 
              ref={graphRef}
              style={{ 
                transform: `scale(${zoom})`, 
                transformOrigin: 'top center',
                display: 'flex' 
              }}
            >
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App