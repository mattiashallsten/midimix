MidiMix {
	var knobs, knobLabels, faders, faderLabels, faderLabelsOver, masterFader, masterFaderLabel, buttons, buttonLabels;
	var smallfont, bigfont;
	var <mOut, <mUid, midifuncs;
	var window;

	// *** Class method: new
	*new {
		^super.new.init
	}

	// *** Instance method: init
	init {
		smallfont = Font.monospace(12);
		bigfont = Font.monospace(36);
		
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

		buttons = [
			{Button()}!9,
			{Button()}!8
		];
		buttonLabels = [
			{StaticText().font_(smallfont).align_(\center)}!9,
			{StaticText().font_(smallfont).align_(\center)}!8,
		];

		this.setupMIDI();
	}

	// *** Instance method: setupMIDI
	setupMIDI {
		MIDIClient.init( verbose: false );
		MIDIIn.connectAll;

		this.findAndConnectMIDIMix();
		
		midifuncs = [];

		midifuncs = midifuncs.add(
			MIDIFunc.cc({|val, num|
				AppClock.play(Routine{
					if(faders[num].notNil, {
						faders[num].valueAction_(val / 127)
					});
					if(knobs[0][num - 24].notNil, {
						knobs[0][num - 24].valueAction_(val / 127)
					});
					if(knobs[1][num - 16].notNil, {
						knobs[1][num - 16].valueAction_(val / 127)
					});
					if(knobs[2][num - 8].notNil, {
						knobs[2][num - 8].valueAction_(val / 127)
					});
				});
				}, srcID: mUid)
		);
	}

	// *** Instance method: findAndConnectMIDIMix
	findAndConnectMIDIMix {
		MIDIClient.destinations.do{|item|
			if(item.device == "MIDI Mix", {
				"MIDI Mix output connected!".postln;
				mOut = item
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

		window = Window.new("MIDI Mix", Rect(200, 200, 800, 600));

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
			HLayout(*buttons[0]),
			HLayout(*buttonLabels[0]),
			HLayout(*(buttons[1] ++ View())),
			HLayout(*buttonLabels[1]),

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
				buttons.clipAt(row).clipAt(col).action = {|button|
					function.value(button.value)
				}
			}
		)
	}

	// *** Instance method: setButtonState
	setButtonState {|col = 0, row = 0, states|
		buttons.clipAt(col).clipAt(row).states_(states)
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


}

// Local Variables:
// eval: (outshine-mode 1)
// End: