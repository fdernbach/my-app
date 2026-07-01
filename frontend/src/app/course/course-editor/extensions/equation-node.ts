import { Node, NodeViewRendererProps } from '@tiptap/core';
import katex from 'katex';
import 'mathlive';

export const EquationNode = Node.create({
  name: 'equation',
  group: 'block',
  atom: true,
  selectable: true,
  draggable: true,

  addAttributes() {
    return {
      latex:       { default: '' },
      displayMode: { default: true },
    };
  },

  parseHTML() {
    return [{ tag: 'div[data-type="equation"]' }];
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', { ...HTMLAttributes, 'data-type': 'equation' }];
  },

  addNodeView() {
    return ({ node, getPos, editor }: NodeViewRendererProps) => {
      const dom = document.createElement('div');
      dom.className = 'ce-equation';
      dom.setAttribute('tabindex', '0');

      // ── KaTeX rendered output ─────────────────────────────────────────────
      const rendered = document.createElement('div');
      rendered.className = 'ce-equation__rendered';

      // ── MathLive editor ───────────────────────────────────────────────────
      const editorWrap = document.createElement('div');
      editorWrap.className = 'ce-equation__editor';
      editorWrap.style.display = 'none';

      const mathField = document.createElement('math-field') as HTMLElement & {
        value: string;
        setValue(v: string, opts?: Record<string, unknown>): void;
      };
      mathField.className = 'ce-equation__mathfield';

      const toolbar = document.createElement('div');
      toolbar.className = 'ce-equation__mf-toolbar';
      const doneBtn = document.createElement('button');
      doneBtn.type = 'button';
      doneBtn.className = 'ce-equation__done-btn';
      doneBtn.textContent = 'Valider ↵';
      toolbar.appendChild(doneBtn);

      editorWrap.appendChild(mathField);
      editorWrap.appendChild(toolbar);

      dom.appendChild(rendered);
      dom.appendChild(editorWrap);

      renderKatex(node.attrs['latex'], rendered, node.attrs['displayMode']);

      let editing = false;

      const openEditor = () => {
        editing = true;
        rendered.style.display = 'none';
        editorWrap.style.display = '';
        if (typeof mathField.setValue === 'function') {
          mathField.setValue(node.attrs['latex'] || '', { suppressChangeNotifications: true });
        } else {
          mathField.value = node.attrs['latex'] || '';
        }
        setTimeout(() => (mathField as any).focus?.(), 50);
      };

      const closeEditor = () => {
        editing = false;
        editorWrap.style.display = 'none';
        rendered.style.display = '';
        const latex = mathField.value || '';
        renderKatex(latex, rendered, node.attrs['displayMode']);
        if (typeof getPos === 'function') {
          const pos = (getPos as () => number)();
          const tr = editor.state.tr;
          tr.setNodeMarkup(pos, undefined, { ...node.attrs, latex });
          editor.view.dispatch(tr);
        }
      };

      dom.addEventListener('dblclick', openEditor);
      doneBtn.addEventListener('click', closeEditor);
      mathField.addEventListener('keydown', (e) => {
        if ((e as KeyboardEvent).key === 'Escape') closeEditor();
        e.stopPropagation();
      });
      mathField.addEventListener('input', (e) => e.stopPropagation());

      return {
        dom,
        stopEvent(_event: Event) { return editing; },
        update(updatedNode) {
          if (updatedNode.type.name !== 'equation') return false;
          if (!editing) {
            renderKatex(updatedNode.attrs['latex'], rendered, updatedNode.attrs['displayMode']);
          }
          return true;
        },
        destroy() {
          dom.removeEventListener('dblclick', openEditor);
        },
      };
    };
  },
});

function renderKatex(latex: string, container: HTMLElement, displayMode: boolean) {
  if (!latex?.trim()) {
    container.innerHTML = '<span class="ce-equation__placeholder">Double-cliquer pour saisir une équation…</span>';
    return;
  }
  try {
    katex.render(latex, container, { displayMode, throwOnError: false, output: 'html' });
  } catch {
    container.textContent = latex;
  }
}
