import{d as ee,I as c,r as A,ax as ut,ay as ye,az as vt,aA as ht,aB as Z,aC as gt,P as xt,a3 as Pe,aD as mt,aE as yt,aF as wt,F as St,aG as Ct,aH as Rt,aI as Tt,s as Q,aJ as zt,V as r,Y as i,U as m,Z as T,aq as $t,aK as ne,a0 as we,aL as oe,J as Pt,a5 as We,aM as Wt,aN as Se,L as Et,y as ie,z as se,D as Lt,M as _t,O as j,aO as At,aP as Bt,a8 as I,aQ as q,a9 as kt,aR as jt,av as It,aS as Ot,aT as Ht,aU as Ft,Q as J}from"./index-Dm1CTX8Q.js";const Mt=ye(".v-x-scroll",{overflow:"auto",scrollbarWidth:"none"},[ye("&::-webkit-scrollbar",{width:0,height:0})]),Dt=ee({name:"XScroll",props:{disabled:Boolean,onScroll:Function},setup(){const e=A(null);function n(d){!(d.currentTarget.offsetWidth<d.currentTarget.scrollWidth)||d.deltaY===0||(d.currentTarget.scrollLeft+=d.deltaY+d.deltaX,d.preventDefault())}const s=ut();return Mt.mount({id:"vueuc/x-scroll",head:!0,anchorMetaName:vt,ssr:s}),Object.assign({selfRef:e,handleWheel:n},{scrollTo(...d){var y;(y=e.value)===null||y===void 0||y.scrollTo(...d)}})},render(){return c("div",{ref:"selfRef",onScroll:this.onScroll,onWheel:this.disabled?void 0:this.handleWheel,class:"v-x-scroll"},this.$slots)}});var Nt=/\s/;function Vt(e){for(var n=e.length;n--&&Nt.test(e.charAt(n)););return n}var Ut=/^\s+/;function Xt(e){return e&&e.slice(0,Vt(e)+1).replace(Ut,"")}var Ce=NaN,Gt=/^[-+]0x[0-9a-f]+$/i,Yt=/^0b[01]+$/i,Kt=/^0o[0-7]+$/i,qt=parseInt;function Re(e){if(typeof e=="number")return e;if(ht(e))return Ce;if(Z(e)){var n=typeof e.valueOf=="function"?e.valueOf():e;e=Z(n)?n+"":n}if(typeof e!="string")return e===0?e:+e;e=Xt(e);var s=Yt.test(e);return s||Kt.test(e)?qt(e.slice(2),s?2:8):Gt.test(e)?Ce:+e}var le=function(){return gt.Date.now()},Jt="Expected a function",Qt=Math.max,Zt=Math.min;function ea(e,n,s){var u,d,y,v,f,x,h=0,g=!1,E=!1,L=!0;if(typeof e!="function")throw new TypeError(Jt);n=Re(n)||0,Z(s)&&(g=!!s.leading,E="maxWait"in s,y=E?Qt(Re(s.maxWait)||0,n):y,L="trailing"in s?!!s.trailing:L);function w(b){var R=u,F=d;return u=d=void 0,h=b,v=e.apply(F,R),v}function z(b){return h=b,f=setTimeout(W,n),g?w(b):v}function C(b){var R=b-x,F=b-h,X=n-R;return E?Zt(X,y-F):X}function P(b){var R=b-x,F=b-h;return x===void 0||R>=n||R<0||E&&F>=y}function W(){var b=le();if(P(b))return $(b);f=setTimeout(W,C(b))}function $(b){return f=void 0,L&&u?w(b):(u=d=void 0,v)}function H(){f!==void 0&&clearTimeout(f),h=0,u=x=d=f=void 0}function _(){return f===void 0?v:$(le())}function p(){var b=le(),R=P(b);if(u=arguments,d=this,x=b,R){if(f===void 0)return z(x);if(E)return clearTimeout(f),f=setTimeout(W,n),w(x)}return f===void 0&&(f=setTimeout(W,n)),v}return p.cancel=H,p.flush=_,p}var ta="Expected a function";function aa(e,n,s){var u=!0,d=!0;if(typeof e!="function")throw new TypeError(ta);return Z(s)&&(u="leading"in s?!!s.leading:u,d="trailing"in s?!!s.trailing:d),ea(e,n,{leading:u,maxWait:n,trailing:d})}const fe=xt("n-tabs"),Ee={tab:[String,Number,Object,Function],name:{type:[String,Number],required:!0},disabled:Boolean,displayDirective:{type:String,default:"if"},closable:{type:Boolean,default:void 0},tabProps:Object,label:[String,Number,Object,Function]},sa=ee({__TAB_PANE__:!0,name:"TabPane",alias:["TabPanel"],props:Ee,slots:Object,setup(e){const n=Pe(fe,null);return n||mt("tab-pane","`n-tab-pane` must be placed inside `n-tabs`."),{style:n.paneStyleRef,class:n.paneClassRef,mergedClsPrefix:n.mergedClsPrefixRef}},render(){return c("div",{class:[`${this.mergedClsPrefix}-tab-pane`,this.class],style:this.style},this.$slots)}}),ra=Object.assign({internalLeftPadded:Boolean,internalAddable:Boolean,internalCreatedByPane:Boolean},zt(Ee,["displayDirective"])),be=ee({__TAB__:!0,inheritAttrs:!1,name:"Tab",props:ra,setup(e){const{mergedClsPrefixRef:n,valueRef:s,typeRef:u,closableRef:d,tabStyleRef:y,addTabStyleRef:v,tabClassRef:f,addTabClassRef:x,tabChangeIdRef:h,onBeforeLeaveRef:g,triggerRef:E,handleAdd:L,activateTab:w,handleClose:z}=Pe(fe);return{trigger:E,mergedClosable:Q(()=>{if(e.internalAddable)return!1;const{closable:C}=e;return C===void 0?d.value:C}),style:y,addStyle:v,tabClass:f,addTabClass:x,clsPrefix:n,value:s,type:u,handleClose(C){C.stopPropagation(),!e.disabled&&z(e.name)},activateTab(){if(e.disabled)return;if(e.internalAddable){L();return}const{name:C}=e,P=++h.id;if(C!==s.value){const{value:W}=g;W?Promise.resolve(W(e.name,s.value)).then($=>{$&&h.id===P&&w(C)}):w(C)}}}},render(){const{internalAddable:e,clsPrefix:n,name:s,disabled:u,label:d,tab:y,value:v,mergedClosable:f,trigger:x,$slots:{default:h}}=this,g=d??y;return c("div",{class:`${n}-tabs-tab-wrapper`},this.internalLeftPadded?c("div",{class:`${n}-tabs-tab-pad`}):null,c("div",Object.assign({key:s,"data-name":s,"data-disabled":u?!0:void 0},yt({class:[`${n}-tabs-tab`,v===s&&`${n}-tabs-tab--active`,u&&`${n}-tabs-tab--disabled`,f&&`${n}-tabs-tab--closable`,e&&`${n}-tabs-tab--addable`,e?this.addTabClass:this.tabClass],onClick:x==="click"?this.activateTab:void 0,onMouseenter:x==="hover"?this.activateTab:void 0,style:e?this.addStyle:this.style},this.internalCreatedByPane?this.tabProps||{}:this.$attrs)),c("span",{class:`${n}-tabs-tab__label`},e?c(St,null,c("div",{class:`${n}-tabs-tab__height-placeholder`}," "),c(Ct,{clsPrefix:n},{default:()=>c(Rt,null)})):h?h():typeof g=="object"?g:wt(g??s)),f&&this.type==="card"?c(Tt,{clsPrefix:n,class:`${n}-tabs-tab__close`,onClick:this.handleClose,disabled:u}):null))}}),na=r("tabs",`
 box-sizing: border-box;
 width: 100%;
 display: flex;
 flex-direction: column;
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
`,[i("segment-type",[r("tabs-rail",[m("&.transition-disabled",[r("tabs-capsule",`
 transition: none;
 `)])])]),i("top",[r("tab-pane",`
 padding: var(--n-pane-padding-top) var(--n-pane-padding-right) var(--n-pane-padding-bottom) var(--n-pane-padding-left);
 `)]),i("left",[r("tab-pane",`
 padding: var(--n-pane-padding-right) var(--n-pane-padding-bottom) var(--n-pane-padding-left) var(--n-pane-padding-top);
 `)]),i("left, right",`
 flex-direction: row;
 `,[r("tabs-bar",`
 width: 2px;
 right: 0;
 transition:
 top .2s var(--n-bezier),
 max-height .2s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `),r("tabs-tab",`
 padding: var(--n-tab-padding-vertical); 
 `)]),i("right",`
 flex-direction: row-reverse;
 `,[r("tab-pane",`
 padding: var(--n-pane-padding-left) var(--n-pane-padding-top) var(--n-pane-padding-right) var(--n-pane-padding-bottom);
 `),r("tabs-bar",`
 left: 0;
 `)]),i("bottom",`
 flex-direction: column-reverse;
 justify-content: flex-end;
 `,[r("tab-pane",`
 padding: var(--n-pane-padding-bottom) var(--n-pane-padding-right) var(--n-pane-padding-top) var(--n-pane-padding-left);
 `),r("tabs-bar",`
 top: 0;
 `)]),r("tabs-rail",`
 position: relative;
 padding: 3px;
 border-radius: var(--n-tab-border-radius);
 width: 100%;
 background-color: var(--n-color-segment);
 transition: background-color .3s var(--n-bezier);
 display: flex;
 align-items: center;
 `,[r("tabs-capsule",`
 border-radius: var(--n-tab-border-radius);
 position: absolute;
 pointer-events: none;
 background-color: var(--n-tab-color-segment);
 box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .08);
 transition: transform 0.3s var(--n-bezier);
 `),r("tabs-tab-wrapper",`
 flex-basis: 0;
 flex-grow: 1;
 display: flex;
 align-items: center;
 justify-content: center;
 `,[r("tabs-tab",`
 overflow: hidden;
 border-radius: var(--n-tab-border-radius);
 width: 100%;
 display: flex;
 align-items: center;
 justify-content: center;
 `,[i("active",`
 font-weight: var(--n-font-weight-strong);
 color: var(--n-tab-text-color-active);
 `),m("&:hover",`
 color: var(--n-tab-text-color-hover);
 `)])])]),i("flex",[r("tabs-nav",`
 width: 100%;
 position: relative;
 `,[r("tabs-wrapper",`
 width: 100%;
 `,[r("tabs-tab",`
 margin-right: 0;
 `)])])]),r("tabs-nav",`
 box-sizing: border-box;
 line-height: 1.5;
 display: flex;
 transition: border-color .3s var(--n-bezier);
 `,[T("prefix, suffix",`
 display: flex;
 align-items: center;
 `),T("prefix","padding-right: 16px;"),T("suffix","padding-left: 16px;")]),i("top, bottom",[m(">",[r("tabs-nav",[r("tabs-nav-scroll-wrapper",[m("&::before",`
 top: 0;
 bottom: 0;
 left: 0;
 width: 20px;
 `),m("&::after",`
 top: 0;
 bottom: 0;
 right: 0;
 width: 20px;
 `),i("shadow-start",[m("&::before",`
 box-shadow: inset 10px 0 8px -8px rgba(0, 0, 0, .12);
 `)]),i("shadow-end",[m("&::after",`
 box-shadow: inset -10px 0 8px -8px rgba(0, 0, 0, .12);
 `)])])])])]),i("left, right",[r("tabs-nav-scroll-content",`
 flex-direction: column;
 `),m(">",[r("tabs-nav",[r("tabs-nav-scroll-wrapper",[m("&::before",`
 top: 0;
 left: 0;
 right: 0;
 height: 20px;
 `),m("&::after",`
 bottom: 0;
 left: 0;
 right: 0;
 height: 20px;
 `),i("shadow-start",[m("&::before",`
 box-shadow: inset 0 10px 8px -8px rgba(0, 0, 0, .12);
 `)]),i("shadow-end",[m("&::after",`
 box-shadow: inset 0 -10px 8px -8px rgba(0, 0, 0, .12);
 `)])])])])]),r("tabs-nav-scroll-wrapper",`
 flex: 1;
 position: relative;
 overflow: hidden;
 `,[r("tabs-nav-y-scroll",`
 height: 100%;
 width: 100%;
 overflow-y: auto; 
 scrollbar-width: none;
 `,[m("&::-webkit-scrollbar, &::-webkit-scrollbar-track-piece, &::-webkit-scrollbar-thumb",`
 width: 0;
 height: 0;
 display: none;
 `)]),m("&::before, &::after",`
 transition: box-shadow .3s var(--n-bezier);
 pointer-events: none;
 content: "";
 position: absolute;
 z-index: 1;
 `)]),r("tabs-nav-scroll-content",`
 display: flex;
 position: relative;
 min-width: 100%;
 min-height: 100%;
 width: fit-content;
 box-sizing: border-box;
 `),r("tabs-wrapper",`
 display: inline-flex;
 flex-wrap: nowrap;
 position: relative;
 `),r("tabs-tab-wrapper",`
 display: flex;
 flex-wrap: nowrap;
 flex-shrink: 0;
 flex-grow: 0;
 `),r("tabs-tab",`
 cursor: pointer;
 white-space: nowrap;
 flex-wrap: nowrap;
 display: inline-flex;
 align-items: center;
 color: var(--n-tab-text-color);
 font-size: var(--n-tab-font-size);
 background-clip: padding-box;
 padding: var(--n-tab-padding);
 transition:
 box-shadow .3s var(--n-bezier),
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `,[i("disabled",{cursor:"not-allowed"}),T("close",`
 margin-left: 6px;
 transition:
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 `),T("label",`
 display: flex;
 align-items: center;
 z-index: 1;
 `)]),r("tabs-bar",`
 position: absolute;
 bottom: 0;
 height: 2px;
 border-radius: 1px;
 background-color: var(--n-bar-color);
 transition:
 left .2s var(--n-bezier),
 max-width .2s var(--n-bezier),
 opacity .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `,[m("&.transition-disabled",`
 transition: none;
 `),i("disabled",`
 background-color: var(--n-tab-text-color-disabled)
 `)]),r("tabs-pane-wrapper",`
 position: relative;
 overflow: hidden;
 transition: max-height .2s var(--n-bezier);
 `),r("tab-pane",`
 color: var(--n-pane-text-color);
 width: 100%;
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 opacity .2s var(--n-bezier);
 left: 0;
 right: 0;
 top: 0;
 `,[m("&.next-transition-leave-active, &.prev-transition-leave-active, &.next-transition-enter-active, &.prev-transition-enter-active",`
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 transform .2s var(--n-bezier),
 opacity .2s var(--n-bezier);
 `),m("&.next-transition-leave-active, &.prev-transition-leave-active",`
 position: absolute;
 `),m("&.next-transition-enter-from, &.prev-transition-leave-to",`
 transform: translateX(32px);
 opacity: 0;
 `),m("&.next-transition-leave-to, &.prev-transition-enter-from",`
 transform: translateX(-32px);
 opacity: 0;
 `),m("&.next-transition-leave-from, &.next-transition-enter-to, &.prev-transition-leave-from, &.prev-transition-enter-to",`
 transform: translateX(0);
 opacity: 1;
 `)]),r("tabs-tab-pad",`
 box-sizing: border-box;
 width: var(--n-tab-gap);
 flex-grow: 0;
 flex-shrink: 0;
 `),i("line-type, bar-type",[r("tabs-tab",`
 font-weight: var(--n-tab-font-weight);
 box-sizing: border-box;
 vertical-align: bottom;
 `,[m("&:hover",{color:"var(--n-tab-text-color-hover)"}),i("active",`
 color: var(--n-tab-text-color-active);
 font-weight: var(--n-tab-font-weight-active);
 `),i("disabled",{color:"var(--n-tab-text-color-disabled)"})])]),r("tabs-nav",[i("line-type",[i("top",[T("prefix, suffix",`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),r("tabs-nav-scroll-content",`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),r("tabs-bar",`
 bottom: -1px;
 `)]),i("left",[T("prefix, suffix",`
 border-right: 1px solid var(--n-tab-border-color);
 `),r("tabs-nav-scroll-content",`
 border-right: 1px solid var(--n-tab-border-color);
 `),r("tabs-bar",`
 right: -1px;
 `)]),i("right",[T("prefix, suffix",`
 border-left: 1px solid var(--n-tab-border-color);
 `),r("tabs-nav-scroll-content",`
 border-left: 1px solid var(--n-tab-border-color);
 `),r("tabs-bar",`
 left: -1px;
 `)]),i("bottom",[T("prefix, suffix",`
 border-top: 1px solid var(--n-tab-border-color);
 `),r("tabs-nav-scroll-content",`
 border-top: 1px solid var(--n-tab-border-color);
 `),r("tabs-bar",`
 top: -1px;
 `)]),T("prefix, suffix",`
 transition: border-color .3s var(--n-bezier);
 `),r("tabs-nav-scroll-content",`
 transition: border-color .3s var(--n-bezier);
 `),r("tabs-bar",`
 border-radius: 0;
 `)]),i("card-type",[T("prefix, suffix",`
 transition: border-color .3s var(--n-bezier);
 `),r("tabs-pad",`
 flex-grow: 1;
 transition: border-color .3s var(--n-bezier);
 `),r("tabs-tab-pad",`
 transition: border-color .3s var(--n-bezier);
 `),r("tabs-tab",`
 font-weight: var(--n-tab-font-weight);
 border: 1px solid var(--n-tab-border-color);
 background-color: var(--n-tab-color);
 box-sizing: border-box;
 position: relative;
 vertical-align: bottom;
 display: flex;
 justify-content: space-between;
 font-size: var(--n-tab-font-size);
 color: var(--n-tab-text-color);
 `,[i("addable",`
 padding-left: 8px;
 padding-right: 8px;
 font-size: 16px;
 justify-content: center;
 `,[T("height-placeholder",`
 width: 0;
 font-size: var(--n-tab-font-size);
 `),$t("disabled",[m("&:hover",`
 color: var(--n-tab-text-color-hover);
 `)])]),i("closable","padding-right: 8px;"),i("active",`
 background-color: #0000;
 font-weight: var(--n-tab-font-weight-active);
 color: var(--n-tab-text-color-active);
 `),i("disabled","color: var(--n-tab-text-color-disabled);")])]),i("left, right",`
 flex-direction: column; 
 `,[T("prefix, suffix",`
 padding: var(--n-tab-padding-vertical);
 `),r("tabs-wrapper",`
 flex-direction: column;
 `),r("tabs-tab-wrapper",`
 flex-direction: column;
 `,[r("tabs-tab-pad",`
 height: var(--n-tab-gap-vertical);
 width: 100%;
 `)])]),i("top",[i("card-type",[r("tabs-scroll-padding","border-bottom: 1px solid var(--n-tab-border-color);"),T("prefix, suffix",`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),r("tabs-tab",`
 border-top-left-radius: var(--n-tab-border-radius);
 border-top-right-radius: var(--n-tab-border-radius);
 `,[i("active",`
 border-bottom: 1px solid #0000;
 `)]),r("tabs-tab-pad",`
 border-bottom: 1px solid var(--n-tab-border-color);
 `),r("tabs-pad",`
 border-bottom: 1px solid var(--n-tab-border-color);
 `)])]),i("left",[i("card-type",[r("tabs-scroll-padding","border-right: 1px solid var(--n-tab-border-color);"),T("prefix, suffix",`
 border-right: 1px solid var(--n-tab-border-color);
 `),r("tabs-tab",`
 border-top-left-radius: var(--n-tab-border-radius);
 border-bottom-left-radius: var(--n-tab-border-radius);
 `,[i("active",`
 border-right: 1px solid #0000;
 `)]),r("tabs-tab-pad",`
 border-right: 1px solid var(--n-tab-border-color);
 `),r("tabs-pad",`
 border-right: 1px solid var(--n-tab-border-color);
 `)])]),i("right",[i("card-type",[r("tabs-scroll-padding","border-left: 1px solid var(--n-tab-border-color);"),T("prefix, suffix",`
 border-left: 1px solid var(--n-tab-border-color);
 `),r("tabs-tab",`
 border-top-right-radius: var(--n-tab-border-radius);
 border-bottom-right-radius: var(--n-tab-border-radius);
 `,[i("active",`
 border-left: 1px solid #0000;
 `)]),r("tabs-tab-pad",`
 border-left: 1px solid var(--n-tab-border-color);
 `),r("tabs-pad",`
 border-left: 1px solid var(--n-tab-border-color);
 `)])]),i("bottom",[i("card-type",[r("tabs-scroll-padding","border-top: 1px solid var(--n-tab-border-color);"),T("prefix, suffix",`
 border-top: 1px solid var(--n-tab-border-color);
 `),r("tabs-tab",`
 border-bottom-left-radius: var(--n-tab-border-radius);
 border-bottom-right-radius: var(--n-tab-border-radius);
 `,[i("active",`
 border-top: 1px solid #0000;
 `)]),r("tabs-tab-pad",`
 border-top: 1px solid var(--n-tab-border-color);
 `),r("tabs-pad",`
 border-top: 1px solid var(--n-tab-border-color);
 `)])])])]),de=aa,oa=Object.assign(Object.assign({},We.props),{value:[String,Number],defaultValue:[String,Number],trigger:{type:String,default:"click"},type:{type:String,default:"bar"},closable:Boolean,justifyContent:String,size:{type:String,default:"medium"},placement:{type:String,default:"top"},tabStyle:[String,Object],tabClass:String,addTabStyle:[String,Object],addTabClass:String,barWidth:Number,paneClass:String,paneStyle:[String,Object],paneWrapperClass:String,paneWrapperStyle:[String,Object],addable:[Boolean,Object],tabsPadding:{type:Number,default:0},animated:Boolean,onBeforeLeave:Function,onAdd:Function,"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],onClose:[Function,Array],labelSize:String,activeName:[String,Number],onActiveNameChange:[Function,Array]}),la=ee({name:"Tabs",props:oa,slots:Object,setup(e,{slots:n}){var s,u,d,y;const{mergedClsPrefixRef:v,inlineThemeDisabled:f}=Pt(e),x=We("Tabs","-tabs",na,Wt,e,v),h=A(null),g=A(null),E=A(null),L=A(null),w=A(null),z=A(null),C=A(!0),P=A(!0),W=Se(e,["labelSize","size"]),$=Se(e,["activeName","value"]),H=A((u=(s=$.value)!==null&&s!==void 0?s:e.defaultValue)!==null&&u!==void 0?u:n.default?(y=(d=ne(n.default())[0])===null||d===void 0?void 0:d.props)===null||y===void 0?void 0:y.name:null),_=Et($,H),p={id:0},b=Q(()=>{if(!(!e.justifyContent||e.type==="card"))return{display:"flex",justifyContent:e.justifyContent}});ie(_,()=>{p.id=0,G(),ue()});function R(){var t;const{value:a}=_;return a===null?null:(t=h.value)===null||t===void 0?void 0:t.querySelector(`[data-name="${a}"]`)}function F(t){if(e.type==="card")return;const{value:a}=g;if(!a)return;const o=a.style.opacity==="0";if(t){const l=`${v.value}-tabs-bar--disabled`,{barWidth:S,placement:B}=e;if(t.dataset.disabled==="true"?a.classList.add(l):a.classList.remove(l),["top","bottom"].includes(B)){if(pe(["top","maxHeight","height"]),typeof S=="number"&&t.offsetWidth>=S){const k=Math.floor((t.offsetWidth-S)/2)+t.offsetLeft;a.style.left=`${k}px`,a.style.maxWidth=`${S}px`}else a.style.left=`${t.offsetLeft}px`,a.style.maxWidth=`${t.offsetWidth}px`;a.style.width="8192px",o&&(a.style.transition="none"),a.offsetWidth,o&&(a.style.transition="",a.style.opacity="1")}else{if(pe(["left","maxWidth","width"]),typeof S=="number"&&t.offsetHeight>=S){const k=Math.floor((t.offsetHeight-S)/2)+t.offsetTop;a.style.top=`${k}px`,a.style.maxHeight=`${S}px`}else a.style.top=`${t.offsetTop}px`,a.style.maxHeight=`${t.offsetHeight}px`;a.style.height="8192px",o&&(a.style.transition="none"),a.offsetHeight,o&&(a.style.transition="",a.style.opacity="1")}}}function X(){if(e.type==="card")return;const{value:t}=g;t&&(t.style.opacity="0")}function pe(t){const{value:a}=g;if(a)for(const o of t)a.style[o]=""}function G(){if(e.type==="card")return;const t=R();t?F(t):X()}function ue(){var t;const a=(t=w.value)===null||t===void 0?void 0:t.$el;if(!a)return;const o=R();if(!o)return;const{scrollLeft:l,offsetWidth:S}=a,{offsetLeft:B,offsetWidth:k}=o;l>B?a.scrollTo({top:0,left:B,behavior:"smooth"}):B+k>l+S&&a.scrollTo({top:0,left:B+k-S,behavior:"smooth"})}const Y=A(null);let te=0,O=null;function Le(t){const a=Y.value;if(a){te=t.getBoundingClientRect().height;const o=`${te}px`,l=()=>{a.style.height=o,a.style.maxHeight=o};O?(l(),O(),O=null):O=l}}function _e(t){const a=Y.value;if(a){const o=t.getBoundingClientRect().height,l=()=>{document.body.offsetHeight,a.style.maxHeight=`${o}px`,a.style.height=`${Math.max(te,o)}px`};O?(O(),O=null,l()):O=l}}function Ae(){const t=Y.value;if(t){t.style.maxHeight="",t.style.height="";const{paneWrapperStyle:a}=e;if(typeof a=="string")t.style.cssText=a;else if(a){const{maxHeight:o,height:l}=a;o!==void 0&&(t.style.maxHeight=o),l!==void 0&&(t.style.height=l)}}}const ve={value:[]},he=A("next");function Be(t){const a=_.value;let o="next";for(const l of ve.value){if(l===a)break;if(l===t){o="prev";break}}he.value=o,ke(t)}function ke(t){const{onActiveNameChange:a,onUpdateValue:o,"onUpdate:value":l}=e;a&&J(a,t),o&&J(o,t),l&&J(l,t),H.value=t}function je(t){const{onClose:a}=e;a&&J(a,t)}function ge(){const{value:t}=g;if(!t)return;const a="transition-disabled";t.classList.add(a),G(),t.classList.remove(a)}const M=A(null);function ae({transitionDisabled:t}){const a=h.value;if(!a)return;t&&a.classList.add("transition-disabled");const o=R();o&&M.value&&(M.value.style.width=`${o.offsetWidth}px`,M.value.style.height=`${o.offsetHeight}px`,M.value.style.transform=`translateX(${o.offsetLeft-jt(getComputedStyle(a).paddingLeft)}px)`,t&&M.value.offsetWidth),t&&a.classList.remove("transition-disabled")}ie([_],()=>{e.type==="segment"&&se(()=>{ae({transitionDisabled:!1})})}),Lt(()=>{e.type==="segment"&&ae({transitionDisabled:!0})});let xe=0;function Ie(t){var a;if(t.contentRect.width===0&&t.contentRect.height===0||xe===t.contentRect.width)return;xe=t.contentRect.width;const{type:o}=e;if((o==="line"||o==="bar")&&ge(),o!=="segment"){const{placement:l}=e;re((l==="top"||l==="bottom"?(a=w.value)===null||a===void 0?void 0:a.$el:z.value)||null)}}const Oe=de(Ie,64);ie([()=>e.justifyContent,()=>e.size],()=>{se(()=>{const{type:t}=e;(t==="line"||t==="bar")&&ge()})});const D=A(!1);function He(t){var a;const{target:o,contentRect:{width:l,height:S}}=t,B=o.parentElement.parentElement.offsetWidth,k=o.parentElement.parentElement.offsetHeight,{placement:V}=e;if(!D.value)V==="top"||V==="bottom"?B<l&&(D.value=!0):k<S&&(D.value=!0);else{const{value:U}=L;if(!U)return;V==="top"||V==="bottom"?B-l>U.$el.offsetWidth&&(D.value=!1):k-S>U.$el.offsetHeight&&(D.value=!1)}re(((a=w.value)===null||a===void 0?void 0:a.$el)||null)}const Fe=de(He,64);function Me(){const{onAdd:t}=e;t&&t(),se(()=>{const a=R(),{value:o}=w;!a||!o||o.scrollTo({left:a.offsetLeft,top:0,behavior:"smooth"})})}function re(t){if(!t)return;const{placement:a}=e;if(a==="top"||a==="bottom"){const{scrollLeft:o,scrollWidth:l,offsetWidth:S}=t;C.value=o<=0,P.value=o+S>=l}else{const{scrollTop:o,scrollHeight:l,offsetHeight:S}=t;C.value=o<=0,P.value=o+S>=l}}const De=de(t=>{re(t.target)},64);_t(fe,{triggerRef:j(e,"trigger"),tabStyleRef:j(e,"tabStyle"),tabClassRef:j(e,"tabClass"),addTabStyleRef:j(e,"addTabStyle"),addTabClassRef:j(e,"addTabClass"),paneClassRef:j(e,"paneClass"),paneStyleRef:j(e,"paneStyle"),mergedClsPrefixRef:v,typeRef:j(e,"type"),closableRef:j(e,"closable"),valueRef:_,tabChangeIdRef:p,onBeforeLeaveRef:j(e,"onBeforeLeave"),activateTab:Be,handleClose:je,handleAdd:Me}),At(()=>{G(),ue()}),Bt(()=>{const{value:t}=E;if(!t)return;const{value:a}=v,o=`${a}-tabs-nav-scroll-wrapper--shadow-start`,l=`${a}-tabs-nav-scroll-wrapper--shadow-end`;C.value?t.classList.remove(o):t.classList.add(o),P.value?t.classList.remove(l):t.classList.add(l)});const Ne={syncBarPosition:()=>{G()}},Ve=()=>{ae({transitionDisabled:!0})},me=Q(()=>{const{value:t}=W,{type:a}=e,o={card:"Card",bar:"Bar",line:"Line",segment:"Segment"}[a],l=`${t}${o}`,{self:{barColor:S,closeIconColor:B,closeIconColorHover:k,closeIconColorPressed:V,tabColor:U,tabBorderColor:Ue,paneTextColor:Xe,tabFontWeight:Ge,tabBorderRadius:Ye,tabFontWeightActive:Ke,colorSegment:qe,fontWeightStrong:Je,tabColorSegment:Qe,closeSize:Ze,closeIconSize:et,closeColorHover:tt,closeColorPressed:at,closeBorderRadius:rt,[I("panePadding",t)]:K,[I("tabPadding",l)]:nt,[I("tabPaddingVertical",l)]:ot,[I("tabGap",l)]:it,[I("tabGap",`${l}Vertical`)]:st,[I("tabTextColor",a)]:lt,[I("tabTextColorActive",a)]:dt,[I("tabTextColorHover",a)]:ct,[I("tabTextColorDisabled",a)]:bt,[I("tabFontSize",t)]:ft},common:{cubicBezierEaseInOut:pt}}=x.value;return{"--n-bezier":pt,"--n-color-segment":qe,"--n-bar-color":S,"--n-tab-font-size":ft,"--n-tab-text-color":lt,"--n-tab-text-color-active":dt,"--n-tab-text-color-disabled":bt,"--n-tab-text-color-hover":ct,"--n-pane-text-color":Xe,"--n-tab-border-color":Ue,"--n-tab-border-radius":Ye,"--n-close-size":Ze,"--n-close-icon-size":et,"--n-close-color-hover":tt,"--n-close-color-pressed":at,"--n-close-border-radius":rt,"--n-close-icon-color":B,"--n-close-icon-color-hover":k,"--n-close-icon-color-pressed":V,"--n-tab-color":U,"--n-tab-font-weight":Ge,"--n-tab-font-weight-active":Ke,"--n-tab-padding":nt,"--n-tab-padding-vertical":ot,"--n-tab-gap":it,"--n-tab-gap-vertical":st,"--n-pane-padding-left":q(K,"left"),"--n-pane-padding-right":q(K,"right"),"--n-pane-padding-top":q(K,"top"),"--n-pane-padding-bottom":q(K,"bottom"),"--n-font-weight-strong":Je,"--n-tab-color-segment":Qe}}),N=f?kt("tabs",Q(()=>`${W.value[0]}${e.type[0]}`),me,e):void 0;return Object.assign({mergedClsPrefix:v,mergedValue:_,renderedNames:new Set,segmentCapsuleElRef:M,tabsPaneWrapperRef:Y,tabsElRef:h,barElRef:g,addTabInstRef:L,xScrollInstRef:w,scrollWrapperElRef:E,addTabFixed:D,tabWrapperStyle:b,handleNavResize:Oe,mergedSize:W,handleScroll:De,handleTabsResize:Fe,cssVars:f?void 0:me,themeClass:N?.themeClass,animationDirection:he,renderNameListRef:ve,yScrollElRef:z,handleSegmentResize:Ve,onAnimationBeforeLeave:Le,onAnimationEnter:_e,onAnimationAfterEnter:Ae,onRender:N?.onRender},Ne)},render(){const{mergedClsPrefix:e,type:n,placement:s,addTabFixed:u,addable:d,mergedSize:y,renderNameListRef:v,onRender:f,paneWrapperClass:x,paneWrapperStyle:h,$slots:{default:g,prefix:E,suffix:L}}=this;f?.();const w=g?ne(g()).filter(p=>p.type.__TAB_PANE__===!0):[],z=g?ne(g()).filter(p=>p.type.__TAB__===!0):[],C=!z.length,P=n==="card",W=n==="segment",$=!P&&!W&&this.justifyContent;v.value=[];const H=()=>{const p=c("div",{style:this.tabWrapperStyle,class:`${e}-tabs-wrapper`},$?null:c("div",{class:`${e}-tabs-scroll-padding`,style:s==="top"||s==="bottom"?{width:`${this.tabsPadding}px`}:{height:`${this.tabsPadding}px`}}),C?w.map((b,R)=>(v.value.push(b.props.name),ce(c(be,Object.assign({},b.props,{internalCreatedByPane:!0,internalLeftPadded:R!==0&&(!$||$==="center"||$==="start"||$==="end")}),b.children?{default:b.children.tab}:void 0)))):z.map((b,R)=>(v.value.push(b.props.name),ce(R!==0&&!$?$e(b):b))),!u&&d&&P?ze(d,(C?w.length:z.length)!==0):null,$?null:c("div",{class:`${e}-tabs-scroll-padding`,style:{width:`${this.tabsPadding}px`}}));return c("div",{ref:"tabsElRef",class:`${e}-tabs-nav-scroll-content`},P&&d?c(oe,{onResize:this.handleTabsResize},{default:()=>p}):p,P?c("div",{class:`${e}-tabs-pad`}):null,P?null:c("div",{ref:"barElRef",class:`${e}-tabs-bar`}))},_=W?"top":s;return c("div",{class:[`${e}-tabs`,this.themeClass,`${e}-tabs--${n}-type`,`${e}-tabs--${y}-size`,$&&`${e}-tabs--flex`,`${e}-tabs--${_}`],style:this.cssVars},c("div",{class:[`${e}-tabs-nav--${n}-type`,`${e}-tabs-nav--${_}`,`${e}-tabs-nav`]},we(E,p=>p&&c("div",{class:`${e}-tabs-nav__prefix`},p)),W?c(oe,{onResize:this.handleSegmentResize},{default:()=>c("div",{class:`${e}-tabs-rail`,ref:"tabsElRef"},c("div",{class:`${e}-tabs-capsule`,ref:"segmentCapsuleElRef"},c("div",{class:`${e}-tabs-wrapper`},c("div",{class:`${e}-tabs-tab`}))),C?w.map((p,b)=>(v.value.push(p.props.name),c(be,Object.assign({},p.props,{internalCreatedByPane:!0,internalLeftPadded:b!==0}),p.children?{default:p.children.tab}:void 0))):z.map((p,b)=>(v.value.push(p.props.name),b===0?p:$e(p))))}):c(oe,{onResize:this.handleNavResize},{default:()=>c("div",{class:`${e}-tabs-nav-scroll-wrapper`,ref:"scrollWrapperElRef"},["top","bottom"].includes(_)?c(Dt,{ref:"xScrollInstRef",onScroll:this.handleScroll},{default:H}):c("div",{class:`${e}-tabs-nav-y-scroll`,onScroll:this.handleScroll,ref:"yScrollElRef"},H()))}),u&&d&&P?ze(d,!0):null,we(L,p=>p&&c("div",{class:`${e}-tabs-nav__suffix`},p))),C&&(this.animated&&(_==="top"||_==="bottom")?c("div",{ref:"tabsPaneWrapperRef",style:h,class:[`${e}-tabs-pane-wrapper`,x]},Te(w,this.mergedValue,this.renderedNames,this.onAnimationBeforeLeave,this.onAnimationEnter,this.onAnimationAfterEnter,this.animationDirection)):Te(w,this.mergedValue,this.renderedNames)))}});function Te(e,n,s,u,d,y,v){const f=[];return e.forEach(x=>{const{name:h,displayDirective:g,"display-directive":E}=x.props,L=z=>g===z||E===z,w=n===h;if(x.key!==void 0&&(x.key=h),w||L("show")||L("show:lazy")&&s.has(h)){s.has(h)||s.add(h);const z=!L("if");f.push(z?It(x,[[Ot,w]]):x)}}),v?c(Ht,{name:`${v}-transition`,onBeforeLeave:u,onEnter:d,onAfterEnter:y},{default:()=>f}):f}function ze(e,n){return c(be,{ref:"addTabInstRef",key:"__addable",name:"__addable",internalCreatedByPane:!0,internalAddable:!0,internalLeftPadded:n,disabled:typeof e=="object"&&e.disabled})}function $e(e){const n=Ft(e);return n.props?n.props.internalLeftPadded=!0:n.props={internalLeftPadded:!0},n}function ce(e){return Array.isArray(e.dynamicProps)?e.dynamicProps.includes("internalLeftPadded")||e.dynamicProps.push("internalLeftPadded"):e.dynamicProps=["internalLeftPadded"],e}export{sa as N,la as a};
