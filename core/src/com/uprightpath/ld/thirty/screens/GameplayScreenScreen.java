package com.uprightpath.ld.thirty.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.uprightpath.ld.thirty.Controls;
import com.uprightpath.ld.thirty.Main;
import com.uprightpath.ld.thirty.logic.WorldGroup;
import com.uprightpath.ld.thirty.story.Answer;
import com.uprightpath.ld.thirty.story.Dialog;
import com.uprightpath.ld.thirty.story.Story;
import com.uprightpath.ld.thirty.story.TapAnswer;

/**
 * Created by Geo on 8/22/2014.
 */
public class GameplayScreenScreen extends GameScreen {
    private WorldGroup worldGroup;
    private float tick = 1f / 60f;
    private float accum = 0f;
    private Table mainTable;
    private Table dialogTable;
    private Label lblWorldName;
    private ImageButton btnMute;
    private ImageButton btnOptions;
    private DialogWidget dialogWidget;

    public GameplayScreenScreen(Main main) {
        super(main);
        mainTable = new Table();
        mainTable.setFillParent(true);

        Table topTable = new Table(main.getSkin());
        topTable.setBackground(main.getSkin().getDrawable("default-round-large"));
        lblWorldName = new Label("World Test", main.getSkin());
        topTable.add(lblWorldName).fillX().expandX().align(Align.top);
        this.optionWindow = new OptionWindow(this, main.getSkin(), true);
        this.keyAssignWindow = new KeyAssignWindow(this, main.getSkin());

        btnMute = new ImageButton(main.getSkin().getDrawable("sound-on"));
        btnMute.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Main.musicManager.setMute(btnMute.isChecked());
                Main.soundManager.setMute(btnMute.isChecked());
            }
        });
        topTable.add(btnMute);

        btnOptions = new ImageButton(main.getSkin().getDrawable("options-button"));
        btnOptions.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameplayScreenScreen.this.setOptionMenuVisible(true);
            }
        });
        topTable.add(btnOptions);

        mainTable.add(topTable).fillX().row();

        dialogTable = new Table();
        mainTable.add(dialogTable).pad(20).fill().expand().expand();
        stack.addActor(mainTable);
    }

    public void setWorldGroup(WorldGroup worldGroup) {
        this.worldGroup = worldGroup;
    }

    @Override
    protected void renderImplement(float delta) {
        accum += delta;
        if (worldGroup.isTellingStory()) {
            Story story = worldGroup.getStory();
            if (dialogWidget != null) {
                if (dialogWidget.dialog != story.getDialog()) {
                    dialogTable.removeActor(dialogWidget);
                    dialogWidget = new DialogWidget(story.getDialog());
                    dialogTable.add(dialogWidget).expandX().fillX();
                } else {
                    dialogWidget.update();
                }
            } else {
                dialogWidget = new DialogWidget(story.getDialog());
                dialogTable.add(dialogWidget).expandX().fillX();
            }
        } else {
            if (dialogWidget != null) {
                dialogTable.removeActor(dialogWidget);
                dialogWidget = null;
            }
            while (accum > tick) {
                accum -= tick;
                worldGroup.update();
            }
        }
        if (!worldGroup.isComplete()) {
            worldGroup.render(delta);
            lblWorldName.setText(worldGroup.getCurrentWorld().getPlayer().getName() + ": " + worldGroup.getCurrentWorld().getName());
        }
    }

    public class DialogWidget extends Table {
        private Dialog dialog;
        private Window dialogWindow;
        private Table answerTable;
        private Button btnContinue;
        private Array<Button> buttons = new Array<Button>();
        private int currentSelected = 0;

        public DialogWidget(Dialog dialog) {
            Label label;
            this.dialog = dialog;
            dialogWindow = new Window(dialog.getInteractable().getName(), main.getSkin());
            this.add(dialogWindow).expand(true, false).fillX().row();
            dialogWindow.add();
            label = new Label(dialog.getDialog(), main.getSkin());
            dialogWindow.add(label).expand(true, true);
            answerTable = new Table(main.getSkin());
            if (dialog.getAnswers().size == 1 && dialog.getAnswers().get(0) instanceof TapAnswer) {
                btnContinue = new ImageButton(main.getSkin().getDrawable("continue-dialog"));
                btnContinue.addListener(new AnswerClickListener(0));
                dialogWindow.add(btnContinue).align(Align.bottom);
            } else {
                Answer answer;
                Button button;
                dialogWindow.add(new Image(main.getSkin().getDrawable("answer-dialog")));
                for (int i = 0; i < dialog.getAnswers().size; i++) {
                    answer = dialog.getAnswers().get(i);
                    button = new TextButton(answer.getText(), main.getSkin());
                    button.addListener(new AnswerClickListener(i));
                    button.addListener(new AnswerMouseListener(i));
                    buttons.add(button);
                    answerTable.add(button).fill(true, false).row();
                }
                this.add(answerTable).align(Align.right);
            }
            updateSelected(0);
        }

        private void update() {
            for (int i = 0; i < buttons.size; i++) {
                buttons.get(i).setDisabled(!GameplayScreenScreen.this.worldGroup.getStory().canPerform(i));
            }

            if (buttons.size > 0) {
                if (Controls.UP.isJustDown()) {
                    updateSelected((buttons.size + currentSelected - 1) % buttons.size);
                } else if (Controls.DOWN.isJustDown()) {
                    updateSelected((buttons.size + currentSelected + 1) % buttons.size);
                }
            }

            if (Controls.ACCEPT.isJustDown()) {
                if (buttons.size == 0) {
                    btnContinue.toggle();
                } else {
                    if (!buttons.get(currentSelected).isDisabled()) {
                        buttons.get(currentSelected).toggle();
                    }
                }
            }
        }

        public void updateSelected(int id) {
            if (buttons.size > 0) {
                buttons.get(currentSelected).setStyle(main.getSkin().get("default", TextButton.TextButtonStyle.class));
                currentSelected = id;
                buttons.get(currentSelected).setStyle(main.getSkin().get("selected", TextButton.TextButtonStyle.class));
            }
        }

        private void clicked(int i) {
            GameplayScreenScreen.this.worldGroup.getStory().perform(i);
        }

        private class AnswerMouseListener extends ClickListener {
            private int id;

            public AnswerMouseListener(int id) {
                this.id = id;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                DialogWidget.this.updateSelected(id);
            }
        }

        private class AnswerClickListener extends ChangeListener {

            private int id;

            public AnswerClickListener(int id) {
                this.id = id;
            }

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DialogWidget.this.clicked(id);
            }
        }
    }

    public void show() {
        super.show();
        worldGroup.createDisplay();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        worldGroup.createDisplay();
    }

    @Override
    public void update() {

    }

    public void hide() {
        super.hide();
    }
}
