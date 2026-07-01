import { Node, NodeViewRendererProps } from '@tiptap/core';

export type CourseBlockType =
  | 'chapitre' | 'section' | 'sous_section'
  | 'definition' | 'theoreme' | 'demonstration'
  | 'exemple' | 'exercice' | 'solution';

const BLOCK_META: Record<CourseBlockType, { label: string; color: string; hasTitle: boolean }> = {
  chapitre:      { label: 'Chapitre',      color: '#1565c0', hasTitle: true  },
  section:       { label: 'Section',       color: '#2e7d32', hasTitle: true  },
  sous_section:  { label: 'Sous-section',  color: '#4a148c', hasTitle: true  },
  definition:    { label: 'Définition',    color: '#e65100', hasTitle: false },
  theoreme:      { label: 'Théorème',      color: '#880e4f', hasTitle: false },
  demonstration: { label: 'Démonstration', color: '#37474f', hasTitle: false },
  exemple:       { label: 'Exemple',       color: '#00695c', hasTitle: false },
  exercice:      { label: 'Exercice',      color: '#f57f17', hasTitle: false },
  solution:      { label: 'Solution',      color: '#558b2f', hasTitle: false },
};

function makeCourseBlock(name: CourseBlockType): Node {
  const { label, color, hasTitle } = BLOCK_META[name];

  return Node.create({
    name,
    group: 'block',
    content: 'block+',

    addAttributes() {
      return {
        title: { default: '' },
      };
    },

    parseHTML() {
      return [{ tag: `div[data-type="${name}"]` }];
    },

    renderHTML({ HTMLAttributes }) {
      return ['div', { ...HTMLAttributes, 'data-type': name }, 0];
    },

    addNodeView() {
      return ({ node, getPos, editor }: NodeViewRendererProps) => {
        const dom = document.createElement('div');
        dom.className = `ce-block ce-block--${name.replace('_', '-')}`;
        dom.setAttribute('data-type', name);
        dom.style.setProperty('--block-color', color);

        const header = document.createElement('div');
        header.className = 'ce-block__header';

        const labelEl = document.createElement('span');
        labelEl.className = 'ce-block__label';
        labelEl.textContent = label;
        header.appendChild(labelEl);

        let titleEl: HTMLInputElement | null = null;
        if (hasTitle) {
          titleEl = document.createElement('input');
          titleEl.className = 'ce-block__title-input';
          titleEl.placeholder = `Titre du ${label.toLowerCase()}…`;
          titleEl.value = node.attrs['title'] || '';
          titleEl.addEventListener('input', () => {
            if (typeof getPos === 'function') {
              const pos = (getPos as () => number)();
              const tr = editor.state.tr;
              tr.setNodeMarkup(pos, undefined, { ...node.attrs, title: titleEl!.value });
              editor.view.dispatch(tr);
            }
          });
          titleEl.addEventListener('keydown', (e) => e.stopPropagation());
          header.appendChild(titleEl);
        }

        const content = document.createElement('div');
        content.className = 'ce-block__content';

        dom.appendChild(header);
        dom.appendChild(content);

        return {
          dom,
          contentDOM: content,
          stopEvent(event: Event) {
            const target = event.target as HTMLElement;
            return target.tagName === 'INPUT' && target.closest('.ce-block__header') !== null;
          },
          update(updatedNode) {
            if (updatedNode.type.name !== name) return false;
            if (titleEl && updatedNode.attrs['title'] !== titleEl.value) {
              titleEl.value = updatedNode.attrs['title'] || '';
            }
            return true;
          },
        };
      };
    },
  });
}

export const Chapitre      = makeCourseBlock('chapitre');
export const Section       = makeCourseBlock('section');
export const SousSection   = makeCourseBlock('sous_section');
export const Definition    = makeCourseBlock('definition');
export const Theoreme      = makeCourseBlock('theoreme');
export const Demonstration = makeCourseBlock('demonstration');
export const Exemple       = makeCourseBlock('exemple');
export const Exercice      = makeCourseBlock('exercice');
export const Solution      = makeCourseBlock('solution');
