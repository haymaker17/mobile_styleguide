//
//  HotelTextEditorViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"

@interface HotelTextEditorViewController : MobileViewController <UITextFieldDelegate>
{
	NSString            *fromView;
	NSString            *customTitle;
	UITextField         *textField;
    NSString            *enteredText;
    MobileViewController __weak *parentVC;
}

@property (nonatomic, strong) NSString              *enteredText;
@property (nonatomic, strong) NSIndexPath           *fromIndexPath;
@property (nonatomic, strong) NSString				*fromView;
@property (nonatomic, strong) NSString				*customTitle;
@property (nonatomic, strong) IBOutlet UITextField	*textField;
@property (nonatomic, strong) NSString              *placeholderText;
@property (weak, nonatomic) MobileViewController    *parentVC;
@property (nonatomic) BOOL pressedDone;
-(IBAction)didEndOnExit:(id)sender;
-(void)closeView;

@end
