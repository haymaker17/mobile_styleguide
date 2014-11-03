//
//  DecimalPointButton.h
//  ConcurMobile
//
//  Created by Shifan Wu on 08/01/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *	The UIButton that will have the decimal point on it
 */
@interface DecimalPointButton : UIButton {
	
}

+ (DecimalPointButton *) decimalPointButton;

@end

/**
 *  The UIButton that will have the negative sign on it
 */
@interface NegativeButton : UIButton{
    
}

+ (NegativeButton *) negativeButton;

@end

/**
 *	The class used to create the keypad
 */
@interface NumberKeypad : NSObject {
	
	UITextField __weak *currentTextField;
	
	DecimalPointButton *decimalPointButton;
    
    NegativeButton *negativeButton;
	
	NSTimer *showCustomBtnTimer;
}

@property (nonatomic, strong) NSTimer *showCustomBtnTimer;
@property (nonatomic, strong) DecimalPointButton *decimalPointButton;
@property (nonatomic, strong) NegativeButton *negativeButton;

@property (weak) UITextField *currentTextField;

#pragma mark -
#pragma mark Show the keypad

+ (NumberKeypad *) keypadForTextField:(UITextField *)textField;

- (void) removeButtonFromKeyboard;

@end
