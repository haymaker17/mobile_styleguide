//
//  DecimalPointButton.m
//  ConcurMobile
//
//  Created by Shifan Wu on 08/01/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NumberKeypad.h"
#import "ExSystem.h"

static UIImage *backgroundImageDepressed;
static UIImage *backgroundImage;

/**
 *  This class create a negative sign button and draw it on a frame
 */
@implementation NegativeButton

+ (void) initialize
{
    if ([ExSystem is7Plus])
    {
        backgroundImageDepressed = [UIImage imageNamed:@"btn_pressed_bkg_ios7.png"];
        backgroundImage = [UIImage imageNamed:@"btn_negative_decimal_bkground_ios7.png"];
    }
    else
    {
        backgroundImageDepressed = [UIImage imageNamed:@"btn_pressed_bkg.png"];
        backgroundImage = [UIImage imageNamed:@"btn_negative_decimal_bkground.png"];
    }
}

- (id)init
{
    if(self = [super initWithFrame:CGRectMake(0, 480, 52.5, 53)]) { //Initially hidden
		//[super adjustsImageWhenDisabled:NO];
		self.titleLabel.font = [UIFont systemFontOfSize:35];
		[self setTitleColor:[UIColor colorWithRed:77.0f/255.0f green:84.0f/255.0f blue:98.0f/255.0f alpha:1.0] forState:UIControlStateNormal];
        if (![ExSystem is7Plus]) {
            [self setTitleColor:[UIColor whiteColor] forState:UIControlStateHighlighted];
        }
		[self setTitle:@"-" forState:UIControlStateNormal];
		[self setBackgroundImage:backgroundImageDepressed forState:UIControlStateHighlighted];
        [self setBackgroundImage:backgroundImage forState:UIControlStateNormal];
	}
    return self;
}

- (void) drawRect:(CGRect)rect
{
    [super drawRect:rect];
	
	//Bring in the button at same speed as keyboard
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:0.1]; //we lose 0.1 seconds when we display it with timer
    if ([ExSystem is5])
        self.frame = CGRectMake(0, 515, 52.5, 53);
    else
        self.frame = CGRectMake(0, 427, 52.5, 53);
	[UIView commitAnimations];
}

+ (NegativeButton *) negativeButton
{
    NegativeButton *button = [[NegativeButton alloc] init];
    return button;
}
@end

/**
 *  This class create a decimal button and draw it on a frame
 */
@implementation DecimalPointButton

+ (void) initialize {
    if ([ExSystem is7Plus])
    {
        backgroundImageDepressed = [UIImage imageNamed:@"btn_decimal_pressed_bkg_ios7.png"];
    }
}

- (id) init {
	if(self = [super initWithFrame:CGRectMake(52, 480, 53, 53)]) { //Initially hidden
		//[super adjustsImageWhenDisabled:NO];
		self.titleLabel.font = [UIFont systemFontOfSize:35];
        if (![ExSystem is7Plus]) {
            [self setTitleColor:[UIColor whiteColor] forState:UIControlStateHighlighted];
        }
		[self setTitle:[[NSLocale currentLocale] objectForKey:NSLocaleDecimalSeparator] forState:UIControlStateNormal];
		[self setTitleColor:[UIColor colorWithRed:77.0f/255.0f green:84.0f/255.0f blue:98.0f/255.0f alpha:1.0] forState:UIControlStateNormal];
		[self setBackgroundImage:backgroundImageDepressed forState:UIControlStateHighlighted];
	}
	return self;
}

- (void)drawRect:(CGRect)rect {
	[super drawRect:rect];
	
	//Bring in the button at same speed as keyboard
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:0.1]; //we lose 0.1 seconds when we display it with timer
    if ([ExSystem is5])
        self.frame = CGRectMake(52, 515, 53, 53);
    else
        self.frame = CGRectMake(52, 427, 53, 53);
	[UIView commitAnimations];
}

+ (DecimalPointButton *) decimalPointButton {
	DecimalPointButton *button = [[DecimalPointButton alloc] init];
	return button;
}

@end

/**
 *  This class put both negative and decimal buttons together on a keyboard
 */
@implementation NumberKeypad

static NumberKeypad *CTEKeypad;

@synthesize decimalPointButton;
@synthesize negativeButton;
@synthesize showCustomBtnTimer;

@synthesize currentTextField;

- (void) addButton1ToKeyboard:(NegativeButton *)button1 andButton2:(DecimalPointButton *)button2 {
	//Add a button to the top, above all windows
// Get top most window
    UIWindow *keyboardWindow = [[UIApplication sharedApplication] keyWindow] ;

	[keyboardWindow addSubview:button1];
    [keyboardWindow addSubview:button2];
}

//This is executed after a delay from showKeypadForTextField
- (void) addCustomBtnToKeyboard {
    [CTEKeypad addButton1ToKeyboard:CTEKeypad.negativeButton andButton2:CTEKeypad.decimalPointButton];
}

- (void) negativeBtnPressed
{
	//Check to see if there is a - already
	NSString *currentText = currentTextField.text;
	if ([currentText rangeOfString:@"-" options:NSLiteralSearch].length == 0) {
        currentTextField.text = [@"-" stringByAppendingString:currentTextField.text];
	}else {
		//alreay has a - sign
	}
}

- (void) decimalPointPressed {
	//Check to see if there is a . already
	NSString *currentText = currentTextField.text;
    NSString *decimalSeparator = [[NSLocale currentLocale] objectForKey:NSLocaleDecimalSeparator];
	if ([currentText rangeOfString:decimalSeparator options:NSBackwardsSearch].length == 0) {
		currentTextField.text = [currentTextField.text stringByAppendingString:decimalSeparator];
	}else {
		//alreay has a decimal point
	}
}

/*
 Show the keyboard
 */
+ (NumberKeypad *) keypadForTextField:(UITextField *)textField {
	if (!CTEKeypad) {
		CTEKeypad = [[NumberKeypad alloc] init];
        CTEKeypad.negativeButton = [NegativeButton negativeButton];
		CTEKeypad.decimalPointButton = [DecimalPointButton decimalPointButton];
        [CTEKeypad.negativeButton addTarget:CTEKeypad action:@selector(negativeBtnPressed) forControlEvents:UIControlEventTouchUpInside];
		[CTEKeypad.decimalPointButton addTarget:CTEKeypad action:@selector(decimalPointPressed) forControlEvents:UIControlEventTouchUpInside];
	}
	CTEKeypad.currentTextField = textField;
	CTEKeypad.showCustomBtnTimer = [NSTimer timerWithTimeInterval:0.2 target:CTEKeypad selector:@selector(addCustomBtnToKeyboard) userInfo:nil repeats:NO];
	[[NSRunLoop currentRunLoop] addTimer:CTEKeypad.showCustomBtnTimer forMode:NSDefaultRunLoopMode];
	return CTEKeypad;
}

/*
 Hide the keyboard
 */
- (void) removeButtonFromKeyboard {
	[self.showCustomBtnTimer invalidate]; //stop any timers still wanting to show the button
	[self.decimalPointButton removeFromSuperview];
    [self.negativeButton removeFromSuperview];
}

@end

