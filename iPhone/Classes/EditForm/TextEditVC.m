//
//  TextEditVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/15/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "TextEditVC.h"


@implementation TextEditVC
@synthesize txt, lblTip,btnBack, value, tipText, promptText, viewTitle;
@synthesize delegate = _delegate;
@synthesize context;
@synthesize requiresNumericInput, isPassword;
@synthesize errText, lblErr;
@synthesize numberKeyPad;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)setSeedData:(NSString*)val context:(NSObject*)con delegate:(id<TextEditDelegate>) del
                tip:(NSString*) tip title:(NSString*)vTitle prompt:(NSString*)prompt isNumeric:(BOOL)isNumeric  isPassword:(BOOL)fPwd err:(NSString*)err
{
    self.context = con;
    self.value = val;
    self.delegate = del;
    self.tipText = tip;
    self.errText = err;
    self.viewTitle = vTitle;
    self.promptText = prompt;
    self.requiresNumericInput = isNumeric;
    self.isPassword = fPwd;
    //MOB-16901 : by default autocorrect is enabled
    self.disableAutoCorrect = NO;

}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if( [ExSystem is7Plus] )
    {
        [txt setBackgroundColor:[UIColor whiteColor]];
        [txt setBorderStyle:UITextBorderStyleLine];
        txt.layer.borderColor = [[UIColor whiteColor]CGColor];
        txt.layer.borderWidth= 2.0f;
    }

    // Do any additional setup after loading the view from its nib.
    [txt becomeFirstResponder];
    if(value != nil && [value length] > 0)
        txt.text = value;
    txt.placeholder = promptText;
    if (requiresNumericInput)
	{
        if ([UIDevice isPad])
        {
            txt.keyboardType = UIKeyboardTypeNumbersAndPunctuation;
        }
        else
        {
            txt.keyboardType = UIKeyboardTypeNumberPad;
        }
	}
	else 
	{
		txt.keyboardType = UIKeyboardTypeDefault;
		txt.autocapitalizationType = UITextAutocapitalizationTypeSentences;//MOB-3831
		// for MOB-16901
        if (self.disableAutoCorrect) {
              txt.autocorrectionType = UITextAutocorrectionTypeNo;
        }
      

	}
    
    if (isPassword)
        txt.secureTextEntry = YES;

    CGSize maxSize = CGSizeMake(300, 62);
    CGFloat nextY = 70;
    if ([errText lengthIgnoreWhitespace])
    {
        CGSize errSz = [errText sizeWithFont:[UIFont fontWithName:@"HelveticaNeue" size:12.0f] constrainedToSize:maxSize];
        lblErr.frame = CGRectMake(10, nextY, 300, errSz.height);
        lblErr.text = errText;
        nextY = lblErr.frame.size.height + lblErr.frame.origin.y + 2;
    }
    else
        [lblErr setHidden:YES];
    
    CGSize s = [tipText sizeWithFont:[UIFont fontWithName:@"HelveticaNeue" size:12.0f] constrainedToSize:maxSize lineBreakMode:NSLineBreakByWordWrapping];
    
    lblTip.frame = CGRectMake(10, nextY+10, 300, s.height);
    lblTip.text = tipText;
    self.title = viewTitle;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    
    self.txt = nil;
    self.lblTip = nil;
    self.lblErr = nil;
    self.btnBack = nil;
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];

    [self.txt resignFirstResponder];

    if (numberKeyPad)
    {
		/*
		 Hide the number keypad
         */
		[self.numberKeyPad removeButtonFromKeyboard];
		self.numberKeyPad = nil;
	}
}

#pragma mark -
#pragma mark TextField stuff
- (void)textFieldDidBeginEditing:(UITextField *)textField
{
    if (textField.keyboardType == UIKeyboardTypeNumberPad)
    {
		/*
		 Show the numberKeyPad
		 */
		if (!self.numberKeyPad) {
			self.numberKeyPad = [NumberKeypad keypadForTextField:textField];
		}else {
			//if we go from one field to another - just change the textfield, don't reanimate the decimal point button
			self.numberKeyPad.currentTextField = textField;
		}
	}
}

-(void)textFieldDidEndEditing:(UITextField *)textField
{    
    [self.txt resignFirstResponder]; // MOB-8533
    
    if ((self.value != nil || textField.text != nil) && ![self.value isEqualToString:textField.text])
    {
        self.value = textField.text;

        // This is called after viewWillDisappear is called.
        if (self.delegate != nil)
            [self.delegate textUpdated:context withValue:value];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [self.txt resignFirstResponder]; // MOB-8533
    if ((self.value != nil || textField.text != nil) && ![self.value isEqualToString:textField.text])
    {
        self.value = textField.text;
        if (self.delegate != nil)
            [self.delegate textUpdated:context withValue:value];

    }

    self.delegate = nil; // To prevent another textUpdated call in DidEndEditing
    [self.navigationController popViewControllerAnimated:YES];
    return YES;
    
}

@end
