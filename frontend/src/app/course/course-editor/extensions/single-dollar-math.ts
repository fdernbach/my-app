import { Extension, InputRule } from '@tiptap/core';

/**
 * Adds an input rule so that typing $content$ (single dollar) is converted
 * to an inline math node, in addition to the built-in $$content$$ rule.
 */
export const SingleDollarMath = Extension.create({
  name: 'singleDollarMath',

  addInputRules() {
    return [
      new InputRule({
        find: /(?<!\$)\$([^$\n]+?)\$(?!\$)/,
        handler: ({ state, range, match }) => {
          const latex = match[1]?.trim();
          if (!latex) { return; }
          const inlineMathType = state.schema.nodes['inlineMath'];
          if (!inlineMathType) { return; }
          state.tr.replaceWith(range.from, range.to, inlineMathType.create({ latex }));
        },
      }),
    ];
  },
});
