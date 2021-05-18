//////////////////////////////////////
// MIDI Mix SuperCollider interface //
// Mattias Hållsten 2021		    //
//////////////////////////////////////

MIDIMix {
	var knobs, knobLabels, faders, faderLabels, faderLabelsOver;
	var masterFader, masterFaderLabel, buttons, buttonLabels;
	var buttonViews;
	var buttonActiveLayer;
	var buttonTypes, buttonGroups, buttonActions;
	var smallfont, bigfont;
	var <mOut, <mUid, midifuncs, buttonMidiMap;
	var window;

	// *** Class method: new
	*new {|guiOnLoad = false|
		^super.new.init(guiOnLoad)
	}

	// *** Instance method: init
	init {|guiOnLoad = false|
		smallfont = Font.monospace(12);
		bigfont = Font.monospace(36);

		buttonActiveLayer = 0;

		buttonMidiMap = [
			[1, 4, 7, 10, 13, 16, 19, 22],
			[2, 5, 8, 11, 14, 17, 20, 23],
			[3, 6, 9, 12, 15, 18, 21, 24]
		];
		
		knobs = 3.collect{
			{Knob()}!8
		};
		knobLabels = 3.collect{
			{StaticText().font_(smallfont).align_(\center)}!8
		};

		faders = {Slider()}!8;
		faderLabelsOver = 8.collect{|i|
			StaticText()
			.string_((i + 1).asString)
			.font_(smallfont)
			.align_(\center)
		};
		faderLabels = {StaticText().font_(smallfont).align_(\center)}!9;
		
		masterFader = Slider();
		masterFaderLabel = StaticText()
		.string_("Master")
		.font_(smallfont)
		.align_(\center);

		buttons = 3.collect{
			8.collect{
				Button()
				.states_([["", Color.black, Color.red],["", Color.black, Color.green]])
				.font_(smallfont)
			}
		};
		buttonLabels = 3.collect{
			8.collect{
				StaticText()
				.font_(smallfont)
				.align_(\center)
			}
		};
		
		buttonTypes = 'toggle'!8!3;
		buttonActions = nil!8!3;
		buttonGroups = 0!8!3;
		
		buttons.do{|row, i|
			row.do{|button, j|
				button.action = {
					if(buttonActions[i][j].notNil, {
						buttonActions[i][j].value(button.value);
					});

					if(buttonTypes[i][j] == 'select', {
						var group = buttonGroups[i][j];
						buttonGroups.postln;
						group.postln;
						3.do{|x|
							8.do{|y|
								if(
									(buttonTypes[x][y] == 'select') &&
									(buttonGroups[x][y] == group) &&
									(buttons[x][y].value == 1) &&
									(buttons[x][y] != button), {
										"turning off button at %:%\n".postf(x, y);
										buttons[x][y].valueAction_(0)
									})
							}
						};
					});
					
					if((mOut.notNil) && (buttonMidiMap[i][j].notNil), {
						mOut.noteOn(0, buttonMidiMap[i][j], button.value.asInteger * 127)
					})
				}
			}
		};
		
		buttonViews = {View().background_(Color.grey)}!3;
		buttonViews[1].background_(Color.black);

		buttonViews.do{|item, index|
			item.layout = HLayout(*(buttons[index] ++ View()))
		};

		this.setupMIDI();

		if(guiOnLoad, {
			this.gui;
		});
	}

	// *** Instance method: setupMIDI
	setupMIDI {
		MIDIClient.init( verbose: false );
		MIDIIn.connectAll;

		this.findAndConnectMIDIMix();
		
		midifuncs = [];

		midifuncs = midifuncs.add(
			MIDIFunc.cc({|val, num|
				var value = val / 127;
				AppClock.play(Routine{
					switch(num,
						// Knobs row 1
						16, {knobs[0][0].valueAction_(value)},
						20, {knobs[0][1].valueAction_(value)},
						24, {knobs[0][2].valueAction_(value)},
						28, {knobs[0][3].valueAction_(value)},
						46, {knobs[0][4].valueAction_(value)},
						50, {knobs[0][5].valueAction_(value)},
						54, {knobs[0][6].valueAction_(value)},
						58, {knobs[0][7].valueAction_(value)},
						// Knobs row 2
						17, {knobs[1][0].valueAction_(value)},
						21, {knobs[1][1].valueAction_(value)},
						25, {knobs[1][2].valueAction_(value)},
						29, {knobs[1][3].valueAction_(value)},
						47, {knobs[1][4].valueAction_(value)},
						51, {knobs[1][5].valueAction_(value)},
						55, {knobs[1][6].valueAction_(value)},
						59, {knobs[1][7].valueAction_(value)},
						// Knobs row 3
						18, {knobs[2][0].valueAction_(value)},
						22, {knobs[2][1].valueAction_(value)},
						26, {knobs[2][2].valueAction_(value)},
						30, {knobs[2][3].valueAction_(value)},
						48, {knobs[2][4].valueAction_(value)},
						52, {knobs[2][5].valueAction_(value)},
						56, {knobs[2][6].valueAction_(value)},
						60, {knobs[2][7].valueAction_(value)},
						// Faders
						19, {faders[0].valueAction_(value)},
						23, {faders[1].valueAction_(value)},
						27, {faders[2].valueAction_(value)},
						31, {faders[3].valueAction_(value)},
						49, {faders[4].valueAction_(value)},
						53, {faders[5].valueAction_(value)},
						57, {faders[6].valueAction_(value)},
						61, {faders[7].valueAction_(value)},
						// master fader
						62, {masterFader.valueAction_(value)}
					);
				});
				}, srcID: mUid)
		);

		midifuncs = midifuncs.add(
			MIDIFunc.noteOn({|val, num|
				var row = nil, col = nil;
				switch(num,
					// Row 1
					1, {row = 0; col = 0},
					4, {row = 0; col = 1},
					7, {row = 0; col = 2},
					10, {row = 0; col = 3},
					13, {row = 0; col = 4},
					16, {row = 0; col = 5},
					19, {row = 0; col = 6},
					22, {row = 0; col = 7},
					// Row 2
					2, {row = 1; col = 0},
					5, {row = 1; col = 1},
					8, {row = 1; col = 2},
					11, {row = 1; col = 3},
					14, {row = 1; col = 4},
					17, {row = 1; col = 5},
					20, {row = 1; col = 6},
					23, {row = 1; col = 7},
					// Row 3
					3, {row = 2; col = 0},
					6, {row = 2; col = 1},
					9, {row = 2; col = 2},
					12, {row = 2; col = 3},
					15, {row = 2; col = 4},
					18, {row = 2; col = 5},
					21, {row = 2; col = 6},
					24, {row = 2; col = 7},

				);

				if((row.notNil) && (col.notNil), {
					switch(buttonTypes[row][col],
						'toggle', {
							AppClock.play(Routine{
								var curr = buttons[row][col].value;
								if(curr == 1, {
									buttons[row][col].valueAction_(0);
								}, {
									buttons[row][col].valueAction_(1)
								})
							})
						},
						'mom', {
							AppClock.play(Routine{
								buttons[row][col].valueAction_(1);
							});
						}
					);
				});

				if(num == 27, {
					buttonActiveLayer = 1;
					this.updateMidiButtons;
					AppClock.play(Routine{
						buttonViews[1].background_(Color.grey);
						buttonViews[0].background_(Color.black);
					});
				});
			}, srcID: mUid);
			
			MIDIFunc.noteOff({|val, num|
				var row = 0, col = 0;
				switch(num,
					// Row 1
					1, {row = 0; col = 0},
					4, {row = 0; col = 1},
					7, {row = 0; col = 2},
					10, {row = 0; col = 3},
					13, {row = 0; col = 4},
					16, {row = 0; col = 5},
					19, {row = 0; col = 22},
					// Row 2
					2, {row = 1; col = 0},
					5, {row = 1; col = 1},
					8, {row = 1; col = 2},
					11, {row = 1; col = 3},
					14, {row = 1; col = 4},
					17, {row = 1; col = 5},
					20, {row = 1; col = 6},
					23, {row = 1; col = 7},
					// Row 3
					3, {row = 2; col = 0},
					6, {row = 2; col = 1},
					9, {row = 2; col = 2},
					12, {row = 2; col = 3},
					15, {row = 2; col = 4},
					18, {row = 2; col = 5},
					21, {row = 2; col = 6},
					24, {row = 2; col = 7}
				);

				if(buttonTypes[row][col] == 'mom', {
					AppClock.play(Routine{
						buttons[row][col].valueAction_(0);
					});
				});

				if(num == 27, {
					buttonActiveLayer = 0;
					this.updateMidiButtons;
					AppClock.play(Routine{
						buttonViews[0].background_(Color.grey);
						buttonViews[1].background_(Color.black);
					});
				});
			}, srcID: mUid);
		)
	}

	// *** Instance method: updateMidiButtons
	updateMidiButtons {
		AppClock.play(Routine{
			if(mOut.notNil, {
				buttons.do{|row, i|
					row.do{|button, j|
						mOut.noteOn(0, buttonMidiMap[i][j], button.value * 127)
					}
				}
			});
		})
	}

	// *** Instance method: findAndConnectMIDIMix
	findAndConnectMIDIMix {
		MIDIClient.destinations.do{|item, index|
			if(item.device == "MIDI Mix", {
				Platform.case(
					\osx, {
						"MIDI Mix output connected!".postln;
						mOut = MIDIOut.newByName("MIDI Mix", "MIDI Mix");
						mOut.latency = 0;
					},
					\windows, {
						"MIDI Mix output connected!".postln;
						mOut = MIDIOut.newByName("MIDI Mix", "MIDI Mix");
						mOut.latency = 0;
					},
					\linux, {
						"MIDI Mix output connected!".postln;
						mOut = MIDIOut(0);
						mOut.connect(index);
						mOut.latency = 0;
					}
				)
			})
		};

		MIDIClient.sources.do{|item|
			if(item.device == "MIDI Mix", {
				mUid = item.uid
			})
		};
	}

	// *** Instance method: gui
	gui {
		if(window.notNil, {
			this.closeGui()
		});

		window = Window.new("MIDI Mix", Rect(200, 200, 1000, 800));

		window.layout = VLayout(
			StaticText().string_("MIDI Mix").font_(bigfont).align_(\center),
			HLayout(
				StaticText()
				.font_(smallfont)
				.string_("MIDI In status: " ++ if(mUid.notNil, {"ON"}, {"OFF"})),

				StaticText()
				.font_(smallfont)
				.string_("MIDI Out status: " ++ if(mOut.notNil, {"ON"}, {"OFF"})),
			),
			// Knobs
			HLayout(*(knobs[0] ++ View())),
			HLayout(*(knobLabels[0] ++ View())),
			HLayout(*(knobs[1] ++ View())),
			HLayout(*(knobLabels[1] ++ View())),
			HLayout(*(knobs[2] ++ View())),
			HLayout(*(knobLabels[2] ++ View())),

			// Buttons
			//HLayout(*buttons[0]),
			HLayout(buttonViews[0]),
			HLayout(*buttonLabels[0]),
			HLayout(buttonViews[1]),
			HLayout(*buttonLabels[1]),
			HLayout(buttonViews[2]),

			// Faders
			HLayout(*(faderLabelsOver ++ masterFaderLabel)),
			HLayout(*(faders ++ masterFader)),
			HLayout(*faderLabels);
		);

		window.onClose = {};

		window.front;
	}

	// *** Instance method: closeGui
	closeGui {
		window.close;
		window = nil;
	}

	// *** Instance method: asView
	asView {|parent|
		var view = View(parent);

		view.background_(Color.grey);

		view.layout = VLayout(
			StaticText().string_("MIDI Mix").font_(bigfont).align_(\center),
			HLayout(
				StaticText()
				.font_(smallfont)
				.string_("MIDI In status: " ++ if(mUid.notNil, {"ON"}, {"OFF"})),

				StaticText()
				.font_(smallfont)
				.string_("MIDI Out status: " ++ if(mOut.notNil, {"ON"}, {"OFF"})),
			),
			// Knobs
			HLayout(*(knobs[0] ++ View())),
			HLayout(*(knobLabels[0] ++ View())),
			HLayout(*(knobs[1] ++ View())),
			HLayout(*(knobLabels[1] ++ View())),
			HLayout(*(knobs[2] ++ View())),
			HLayout(*(knobLabels[2] ++ View())),

			// Buttons
			HLayout(*buttons[0]),
			HLayout(*buttonLabels[0]),
			HLayout(*(buttons[1] ++ View())),
			HLayout(*buttonLabels[1]),

			// Faders
			HLayout(*(faderLabelsOver ++ masterFaderLabel)),
			HLayout(*(faders ++ masterFader)),
			HLayout(*faderLabels);
		);

		^view
	}

	// *** Instance method: map
	map {|item = 'knob', row, col, function|
		switch(item,
			'knob', {
				knobs.clipAt(row).clipAt(col).action = {|knob|
					function.value(knob.value)
				};
			},
			'fader', {
				faders.clipAt(col).action = {|fader|
					function.value(fader.value)
				}
			},
			'button', {
				buttonActions.clipAt(row).clipPut(col, function);
				// buttons.clipAt(row).clipAt(col).action = {|button|
				// 	function.value(button.value)
				// }
			}
		)
	}

	// *** Instance method: setButtonStates
	setButtonStates {|col = 0, row = 0, states|
		buttons.clipAt(col).clipAt(row).states_(states)
	}

	// *** Instance method: setButtonType
	setButtonType {|row = 0, col = 0, type|
		buttonTypes.clipAt(row).clipPut(col, type)
	}

	// *** Instance method: setAllButtonTypes
	setAllButtonTypes {|type|
		3.do{|i| 8.do{|j| buttonTypes[i][j] = type } }
	}

	// *** Instance method: setLabel
	setLabel {|item = 'knob', row, col, string|
		switch(item,
			'knob', {
				knobLabels.clipAt(row).clipAt(col).string_(string)
			},
			'fader', {
				faderLabels.clipAt(col).string_(string)
			},
			'button', {
				buttonLabels.clipAt(row).clipAt(col).string_(string)
			}
		)
	}

	// *** Instance method: getButtonValue
	getButtonValue {|row, col|
		^buttons.clipAt(row).clipAt(col).value
	}

	// *** Instance method: setButtonValue
	setButtonValue {|row, col, val|
		buttons.clipAt(row).clipAt(col).valueAction_(val)
	}

	// *** Instance method: setValue
	setValue {|item = 'knob', row, col, val = 0|
		switch(item,
			'knob', {
				knobs.clipAt(row).clipAt(col).valueAction_(val)
			},
			'fader', {
				faders.clipAt(col).valueAction_(val)
			},
			'button', {
				buttons.clipAt(row).clipAt(col).valueAction_(val)
			}
		)
	}


}

// Local Variables:
// eval: (outshine-mode 1)
// End: