import { useState, useRef } from 'react'
import './App.css'

function App() {
  const [code, setCode] = useState("x := 10 + 5 * 2;\nprint x;");
  const [status, setStatus] = useState("Ready to compile");
  const [tokens, setTokens] = useState([]);
  const [isSuccess, setIsSuccess] = useState(true);
  const [zoom, setZoom] = useState(1);
  
  const graphRef = useRef(null);

  const handleCompile = async () => {
    setStatus("Compiling...");
    
    try {
      const response = await fetch("http://localhost:8080/api/compile", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sourceCode: code })
      });
      
      const data = await response.json();
      
      if (data.success) {
        setIsSuccess(true);
        setStatus(data.messages[0]);
        setTokens(data.tokens || []);
        
        if (data.astDot && window.Viz) {
          const viz = new window.Viz();
          viz.renderSVGElement(data.astDot)
            .then(svg => {
              // Ensure SVG doesn't strictly lock its width/height so we can zoom it
              svg.style.width = "100%";
              svg.style.height = "100%";
              
              graphRef.current.innerHTML = ""; 
              graphRef.current.appendChild(svg);
              setZoom(1); // Reset zoom on new compile
            })
            .catch(error => {
              console.error("Graph rendering error:", error);
              setStatus("❌ Failed to draw graph");
            });
        }
      } else {
        setIsSuccess(false);
        setStatus("❌ " + data.messages[0]);
        setTokens([]);
        if (graphRef.current) graphRef.current.innerHTML = ""; 
      }
    } catch (err) {
      setIsSuccess(false);
      setStatus("🚨 Cannot connect to Java API on port 8080");
    }
  };

  // Feature: Download the SVG to the user's computer
  const downloadSvg = () => {
    const svg = graphRef.current.querySelector('svg');
    if (!svg) return alert("No graph to download!");
    
    const serializer = new XMLSerializer();
    const source = serializer.serializeToString(svg);
    const blob = new Blob([source], {type: "image/svg+xml;charset=utf-8"});
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "tinycalc-ast.svg";
    link.click();
  };

  return (
    <div className="app-container">
      {/* TOP NAVIGATION BAR */}
      <header className="top-bar">
        <div className="logo">⚙️ TinyCalc IDE</div>
        <div className={`status-badge ${isSuccess ? 'success' : 'error'}`}>
          {status}
        </div>
        <button onClick={handleCompile} className="compile-btn">
          ▶ Run Compiler
        </button>
      </header>

      {/* 3-COLUMN WORKSPACE */}
      <div className="workspace">
        
        {/* COLUMN 1: CODE EDITOR */}
        <div className="panel editor-panel">
          <div className="panel-header">source.tc</div>
          <textarea 
            className="code-editor"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            spellCheck="false"
          />
        </div>

        {/* COLUMN 2: TOKEN STREAM */}
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

       {/* RIGHT COLUMN: AST GRAPH VIEW */}
       <div className="panel ast-panel">
          <div className="panel-header ast-header">
            <span>Abstract Syntax Tree</span>
            <div className="toolbar">
              <button onClick={() => setZoom(z => z - 0.2)}>➖</button>
              <span className="zoom-level">{Math.round(zoom * 100)}%</span>
              <button onClick={() => setZoom(z => z + 0.2)}>➕</button>
              <button onClick={downloadSvg} className="dl-btn">💾 Export SVG</button>
            </div>
          </div>
          
          <div className="graph-viewport">
            {/* React controls this text safely outside the Viz.js container */}
            {!isSuccess && (
               <p className="empty-text" style={{position: 'absolute', width: '100%'}}>
                 Awaiting successful compilation...
               </p>
            )}
            
            {/* Viz.js gets this completely empty div to play in. React will never touch its children. */}
            <div 
              className="graph-canvas" 
              ref={graphRef}
              style={{ 
                transform: `scale(${zoom})`, 
                transformOrigin: 'top center',
                display: isSuccess ? 'flex' : 'none' 
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