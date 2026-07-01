import {
  Component, ElementRef, EventEmitter, Input, OnDestroy, AfterViewInit,
  Output, ViewChild, ViewEncapsulation,
} from '@angular/core';
import { Editor } from '@tiptap/core';
import { StarterKit } from '@tiptap/starter-kit';
import { Mathematics } from '@tiptap/extension-mathematics';
import { Underline } from '@tiptap/extension-underline';
import { Table } from '@tiptap/extension-table';
import { TableRow } from '@tiptap/extension-table-row';
import { TableCell } from '@tiptap/extension-table-cell';
import { TableHeader } from '@tiptap/extension-table-header';

import {
  Chapitre, Section, SousSection,
  Definition, Theoreme, Demonstration,
  Exemple, Exercice, Solution,
} from './extensions/course-blocks';
import { EquationNode } from './extensions/equation-node';
import { ImageNode } from './extensions/image-node';
import { SingleDollarMath } from './extensions/single-dollar-math';

type BlockCommand =
  | 'chapitre' | 'section' | 'sous_section'
  | 'definition' | 'theoreme' | 'demonstration'
  | 'exemple' | 'exercice' | 'solution'
  | 'equation' | 'image_block'
  | 'bulletList' | 'orderedList' | 'insertTable';

@Component({
  selector: 'app-course-editor',
  standalone: true,
  imports: [],
  templateUrl: './course-editor.component.html',
  styleUrl: './course-editor.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class CourseEditorComponent implements AfterViewInit, OnDestroy {
  @ViewChild('editorEl') editorEl!: ElementRef<HTMLDivElement>;

  @Input() set content(val: Record<string, unknown> | null | undefined) {
    this._initialContent = val ?? null;
    if (this.editor && val) {
      this.editor.commands.setContent(val as any);
    }
  }

  @Output() contentChange = new EventEmitter<Record<string, unknown>>();

  private _initialContent: Record<string, unknown> | null = null;
  editor?: Editor;

  readonly structuralBlocks: { cmd: BlockCommand; label: string }[] = [
    { cmd: 'chapitre',    label: 'Chapitre'     },
    { cmd: 'section',     label: 'Section'      },
    { cmd: 'sous_section',label: 'Sous-section' },
  ];

  readonly contentBlocks: { cmd: BlockCommand; label: string }[] = [
    { cmd: 'definition',    label: 'Définition'   },
    { cmd: 'theoreme',      label: 'Théorème'     },
    { cmd: 'demonstration', label: 'Démo'         },
    { cmd: 'exemple',       label: 'Exemple'      },
    { cmd: 'exercice',      label: 'Exercice'     },
    { cmd: 'solution',      label: 'Solution'     },
  ];

  readonly specialBlocks: { cmd: BlockCommand; label: string; title?: string }[] = [
    { cmd: 'equation',    label: '∑ Équation',   title: 'Insérer un bloc équation'        },
    { cmd: 'image_block', label: '🖼 Image',      title: 'Insérer une image'               },
    { cmd: 'bulletList',  label: '• Liste',      title: 'Liste à puces'                   },
    { cmd: 'orderedList', label: '1. Numérotée', title: 'Liste numérotée'                 },
    { cmd: 'insertTable', label: '⊞ Tableau',    title: 'Insérer un tableau'              },
  ];

  readonly mathShortcuts: { label: string; latex: string; title: string }[] = [
    { label: 'ℝ',  latex: '\\mathbb{R}', title: '\\mathbb{R}'  },
    { label: 'ℕ',  latex: '\\mathbb{N}', title: '\\mathbb{N}'  },
    { label: 'ℤ',  latex: '\\mathbb{Z}', title: '\\mathbb{Z}'  },
    { label: 'ℚ',  latex: '\\mathbb{Q}', title: '\\mathbb{Q}'  },
    { label: 'ℂ',  latex: '\\mathbb{C}', title: '\\mathbb{C}'  },
    { label: '∞',  latex: '\\infty',     title: '\\infty'      },
    { label: '∈',  latex: '\\in',        title: '\\in'         },
    { label: '∉',  latex: '\\notin',     title: '\\notin'      },
    { label: '⊂',  latex: '\\subset',    title: '\\subset'     },
    { label: '⊆',  latex: '\\subseteq',  title: '\\subseteq'   },
    { label: '∅',  latex: '\\emptyset',  title: '\\emptyset'   },
    { label: '∀',  latex: '\\forall',    title: '\\forall'     },
    { label: '∃',  latex: '\\exists',    title: '\\exists'     },
    { label: '⇒',  latex: '\\Rightarrow', title: '\\Rightarrow' },
    { label: '⟺',  latex: '\\Leftrightarrow', title: '\\Leftrightarrow' },
    { label: 'α',  latex: '\\alpha',     title: '\\alpha'      },
    { label: 'β',  latex: '\\beta',      title: '\\beta'       },
    { label: 'γ',  latex: '\\gamma',     title: '\\gamma'      },
    { label: 'δ',  latex: '\\delta',     title: '\\delta'      },
    { label: 'ε',  latex: '\\varepsilon', title: '\\varepsilon' },
    { label: 'λ',  latex: '\\lambda',    title: '\\lambda'     },
    { label: 'π',  latex: '\\pi',        title: '\\pi'         },
    { label: 'σ',  latex: '\\sigma',     title: '\\sigma'      },
    { label: 'θ',  latex: '\\theta',     title: '\\theta'      },
    { label: '±',  latex: '\\pm',        title: '\\pm'         },
  ];

  ngAfterViewInit(): void {
    this.editor = new Editor({
      element: this.editorEl.nativeElement,
      extensions: [
        StarterKit.configure({ heading: { levels: [1, 2, 3] } }),
        Underline,
        Mathematics.configure({
          katexOptions: { throwOnError: false },
          inlineOptions: {
            onClick: (_node, pos) => {
              const current = this.editor?.state.doc.nodeAt(pos);
              const latex = current?.attrs?.['latex'] ?? '';
              const newLatex = window.prompt('Modifier la formule LaTeX :', latex);
              if (newLatex !== null && this.editor) {
                (this.editor.chain().focus() as any)
                  .updateInlineMath({ latex: newLatex, pos })
                  .run();
              }
            },
          },
        }),
        Table.configure({ resizable: true }),
        TableRow,
        TableCell,
        TableHeader,
        Chapitre,
        Section,
        SousSection,
        Definition,
        Theoreme,
        Demonstration,
        Exemple,
        Exercice,
        Solution,
        EquationNode,
        ImageNode,
        SingleDollarMath,
      ],
      content: this._initialContent ?? '',
      onUpdate: ({ editor }) => {
        this.contentChange.emit(editor.getJSON() as Record<string, unknown>);
      },
    });
  }

  ngOnDestroy(): void {
    this.editor?.destroy();
  }

  insertMathShortcut(latex: string): void {
    if (!this.editor) return;
    (this.editor.chain().focus() as any).insertInlineMath({ latex }).run();
  }

  toggleBold()      { this.editor?.chain().focus().toggleBold().run(); }
  toggleItalic()    { this.editor?.chain().focus().toggleItalic().run(); }
  toggleUnderline() { this.editor?.chain().focus().toggleUnderline().run(); }
  toggleStrike()    { this.editor?.chain().focus().toggleStrike().run(); }
  undo()         { this.editor?.chain().focus().undo().run(); }
  redo()         { this.editor?.chain().focus().redo().run(); }

  isActive(mark: string): boolean {
    return this.editor?.isActive(mark) ?? false;
  }

  insertBlock(cmd: BlockCommand): void {
    if (!this.editor) return;

    switch (cmd) {
      case 'bulletList':
        this.editor.chain().focus().toggleBulletList().run();
        return;
      case 'orderedList':
        this.editor.chain().focus().toggleOrderedList().run();
        return;
      case 'insertTable':
        (this.editor.chain().focus() as any).insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run();
        return;
    }

    // For course-specific blocks: insert after the current leaf block if cursor is inside one.
    // This allows "Démonstration" to appear as a sibling of "Théorème", not nested inside it.
    const LEAF_BLOCKS = new Set([
      'definition', 'theoreme', 'demonstration',
      'exemple', 'exercice', 'solution',
      'equation', 'image_block',
    ]);

    const { state } = this.editor;
    const { $from } = state.selection;
    const schema = state.schema;

    // Walk up from cursor to find if we're inside a leaf block
    let insertAfterPos: number | null = null;
    for (let depth = $from.depth; depth >= 1; depth--) {
      if (LEAF_BLOCKS.has($from.node(depth).type.name)) {
        insertAfterPos = $from.after(depth); // position just after the leaf block
        break;
      }
    }

    const nodeType = schema.nodes[cmd];
    if (!nodeType) return;

    const isAtom = cmd === 'equation' || cmd === 'image_block';
    const newNode = isAtom
      ? nodeType.createAndFill({})
      : nodeType.createAndFill({ title: '' }, schema.nodes['paragraph'].create());

    if (!newNode) return;

    const tr = state.tr;
    if (insertAfterPos !== null) {
      // Insert as sibling after the current leaf block
      tr.insert(insertAfterPos, newNode);
    } else {
      // Insert at cursor position
      tr.replaceSelectionWith(newNode);
    }
    this.editor.view.dispatch(tr.scrollIntoView());
  }
}
