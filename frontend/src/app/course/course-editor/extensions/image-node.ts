import { Node, NodeViewRendererProps } from '@tiptap/core';

export const ImageNode = Node.create({
  name: 'image_block',
  group: 'block',
  atom: true,
  selectable: true,
  draggable: true,

  addAttributes() {
    return {
      src:     { default: '' },
      alt:     { default: '' },
      caption: { default: '' },
    };
  },

  parseHTML() {
    return [{ tag: 'figure[data-type="image_block"]' }];
  },

  renderHTML({ HTMLAttributes }) {
    return ['figure', { ...HTMLAttributes, 'data-type': 'image_block' }];
  },

  addNodeView() {
    return ({ node, getPos, editor }: NodeViewRendererProps) => {
      const dom = document.createElement('div');
      dom.className = 'ce-image';

      const updateNode = (attrs: Record<string, string>) => {
        if (typeof getPos === 'function') {
          const pos = (getPos as () => number)();
          const tr = editor.state.tr;
          tr.setNodeMarkup(pos, undefined, { ...node.attrs, ...attrs });
          editor.view.dispatch(tr);
        }
      };

      const urlInput = document.createElement('input');
      urlInput.type = 'url';
      urlInput.className = 'ce-image__url-input';
      urlInput.placeholder = "URL de l'image…";
      urlInput.value = node.attrs['src'] || '';
      urlInput.addEventListener('input', () => updateNode({ src: urlInput.value }));
      urlInput.addEventListener('keydown', (e) => e.stopPropagation());

      const imgEl = document.createElement('img');
      imgEl.className = 'ce-image__img';
      imgEl.alt = node.attrs['alt'] || '';
      if (node.attrs['src']) {
        imgEl.src = node.attrs['src'];
        imgEl.style.display = '';
      } else {
        imgEl.style.display = 'none';
      }

      urlInput.addEventListener('change', () => {
        if (urlInput.value) {
          imgEl.src = urlInput.value;
          imgEl.style.display = '';
        } else {
          imgEl.style.display = 'none';
        }
      });

      const captionInput = document.createElement('input');
      captionInput.type = 'text';
      captionInput.className = 'ce-image__caption-input';
      captionInput.placeholder = 'Légende (optionnelle)…';
      captionInput.value = node.attrs['caption'] || '';
      captionInput.addEventListener('input', () => updateNode({ caption: captionInput.value }));
      captionInput.addEventListener('keydown', (e) => e.stopPropagation());

      dom.appendChild(urlInput);
      dom.appendChild(imgEl);
      dom.appendChild(captionInput);

      return {
        dom,
        stopEvent() { return true; },
        update(updatedNode) {
          if (updatedNode.type.name !== 'image_block') return false;
          urlInput.value = updatedNode.attrs['src'] || '';
          imgEl.alt = updatedNode.attrs['alt'] || '';
          captionInput.value = updatedNode.attrs['caption'] || '';
          if (updatedNode.attrs['src']) {
            imgEl.src = updatedNode.attrs['src'];
            imgEl.style.display = '';
          } else {
            imgEl.style.display = 'none';
          }
          return true;
        },
      };
    };
  },
});
