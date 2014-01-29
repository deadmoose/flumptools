package com.deadmoose.flumptools.flumpview;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import playn.core.GroupLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.Surface;
import playn.core.util.Callback;
import playn.core.util.Clock;
import playn.java.JavaPlatform;
import pythagoras.f.Point;
import react.UnitSlot;
import react.Value;
import react.ValueView;
import tripleplay.flump.JsonLoader;
import tripleplay.flump.Library;
import tripleplay.flump.Movie;
import tripleplay.flump.MoviePlayer;
import tripleplay.flump.Symbol;
import tripleplay.flump.Texture;
import tripleplay.game.ScreenStack;
import tripleplay.game.UIScreen;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Scroller;
import tripleplay.ui.Shim;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Tabs;
import tripleplay.ui.ToggleButton;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.BorderLayout;
import tripleplay.util.Colors;

import com.google.common.collect.Lists;

public class FlumpViewScreen extends UIScreen
{
    public final Value<Library> library = Value.create(null);

    public FlumpViewScreen (ScreenStack screens)
    {
        _screens = screens;
    }

    @Override public void wasShown ()
    {
        super.wasShown();

        _root = iface.createRoot(new BorderLayout(), SimpleStyles.newSheet(), layer);
        _root.setSize(width(), height());

        Shim shim = new Shim(1, 1) {
            @Override protected void layout () {
                super.layout();

                _flumpLayer.setTranslation(size().width()/2, size().height()/2);
            }
        };
        shim.layer.add(_flumpLayer);
        _root.add(shim.setConstraint(BorderLayout.CENTER));

        _flumpLayer.add(PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surface) {
                // Crosshair at the origin
                surface.setFillColor(Colors.WHITE);
                surface.fillRect(-10, -1, 20, 2);
                surface.fillRect(-1, -10, 2, 20);
            }
        }).setDepth(1));

        _flumpLayer.setHitTester(new Layer.HitTester() {
            @Override public Layer hitTest (Layer layer, Point p) {
                return layer;
            }
        });
        _flumpLayer.addListener(new Pointer.Adapter() {
            @Override public void onPointerStart (Pointer.Event event) {
                _lastX = event.x();
                _lastY = event.y();
            }

            @Override public void onPointerDrag (Pointer.Event event) {
                float dx = _lastX - event.x();
                float dy = _lastY - event.y();

                _flumpLayer.setTranslation(_flumpLayer.tx() - dx, _flumpLayer.ty() - dy);

                _lastX = event.x();
                _lastY = event.y();
            }

            protected float _lastX, _lastY;
        });
        _flumpLayer.add(_textureLayer);

        Button loadButton = new Button("Load Library");
        loadButton.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                openFileChooser();
            }
        });
        _root.add(loadButton.setConstraint(BorderLayout.SOUTH));
        _status = new Label().addStyles(Style.COLOR.is(Colors.RED));
        _root.add(_status.setConstraint(BorderLayout.NORTH));

        _movies = new Group(AxisLayout.vertical().stretchByDefault().offStretch());
        _textures = new Group(AxisLayout.vertical().stretchByDefault().offStretch());

        Tabs tabs = new Tabs();
        tabs.add("Movies", new Scroller(_movies));
        tabs.add("Textures", new Scroller(_textures));

        _root.add(tabs.setConstraint(BorderLayout.EAST));

        openFileChooser();
    }

    @Override public void wasHidden ()
    {
        super.wasHidden();

        iface.destroyRoot(_root);
    }

    @Override public void paint (Clock clock)
    {
        super.paint(clock);
        if (_player != null) {
            _player.paint(clock);
        }
    }

    protected void openFileChooser ()
    {
        if (_chooser != null) {
            _chooser.cancelSelection();
        }

        _chooser = new JFileChooser(PlayN.storage().getItem(PREF_KEY));
        _chooser.setFileFilter(new FileNameExtensionFilter("Flump libraries", "json"));

        int returnVal = _chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            loadFlumpLibrary(_chooser.getSelectedFile());
        }
    }

    protected void loadFlumpLibrary (File file)
    {
        JavaPlatform platform = (JavaPlatform)PlayN.platform();
        platform.setTitle("Flumpview - " + file.getParentFile().getName());
        FileAssets assets = new FileAssets(platform);
        _status.text.update("");
        PlayN.storage().setItem(PREF_KEY, file.getParentFile().getParentFile().getAbsolutePath());
        JsonLoader.loadLibrary(assets, file.getParentFile().getAbsolutePath(), new Callback<Library>() {
            public void onSuccess (final Library library) {
                if (_player != null) {
                    _player.destroy();
                }

                _player = new MoviePlayer(library);
                _flumpLayer.add(_player.layer());

                _movies.destroyAll();
                _textures.destroyAll();

                List<String> movies = Lists.newArrayList();
                List<String> textures = Lists.newArrayList();

                for (Map.Entry<String, Symbol> entry : library.symbols.entrySet()) {
                    if (!entry.getKey().startsWith("~")) {
                        if (entry.getValue() instanceof Movie.Symbol) {
                            movies.add(entry.getKey());
                        } else if (entry.getValue() instanceof Texture.Symbol) {
                            textures.add(entry.getKey());
                        }
                    }
                }

                Collections.sort(movies);
                Collections.sort(textures);

                final List<ToggleButton> buttons = Lists.newArrayList();

                for (final String movie : movies) {
                    final ToggleButton button = new ToggleButton(movie);
                    buttons.add(button);
                    _movies.add(button);
                    button.selected().connect(new ValueView.Listener<Boolean>() {
                        @Override public void onChange (Boolean value, Boolean oldValue) {
                            if (value) {
                                _player.layer().setVisible(true);
                                _textureLayer.setVisible(false);

                                _player.loop(movie);
                            }
                        }
                    });
                }
                for (final String texture : textures) {
                    final ToggleButton button = new ToggleButton(texture);
                    buttons.add(button);
                    _textures.add(button);

                    button.selected().connect(new ValueView.Listener<Boolean>() {
                        @Override public void onChange (Boolean value, Boolean oldValue) {
                            if (value) {
                                _player.layer().setVisible(false);
                                _textureLayer.setVisible(true);

                                _textureLayer.removeAll();
                                _textureLayer.add(library.createTexture(texture).layer());
                            }
                        }
                    });
                }

                for (final ToggleButton button : buttons) {
                    button.selected().connect(new ValueView.Listener<Boolean>() {
                        @Override public void onChange (Boolean value, Boolean oldValue) {
                            if (value) {
                                for (ToggleButton otherButton : buttons) {
                                    if (otherButton != button) {
                                        otherButton.selected().update(false);
                                    }
                                }
                            }
                        }
                    });

                }

                ((ToggleButton)_movies.childAt(0)).selected().update(true);
            }

            public void onFailure (Throwable cause) {
                PlayN.log().error("Error loading flump", cause);
                _status.text.update("Error loading flump: " + cause.getMessage());
            }
        });
    }

    protected Root _root;
    protected ScreenStack _screens;
    protected JFileChooser _chooser;
    protected MoviePlayer _player;
    protected GroupLayer _flumpLayer = PlayN.graphics().createGroupLayer();
    protected GroupLayer _textureLayer = PlayN.graphics().createGroupLayer();
    protected Group _movies;
    protected Group _textures;
    protected Label _status;

    protected static final String PREF_KEY = "FlumpViewPath";
}
