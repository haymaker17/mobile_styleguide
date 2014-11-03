//
//  TextEditVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/15/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TextEditDelegate.h"
#import "MobileViewController.h"
#import "NumberKeypad.h"

@interface TextEditVC : MobileViewController <UITextFieldDelegate>{
    UITextField             *txt;
    UILabel                 *lblTip;
    UILabel                 *lblErr;
    UIButton                *btnBack;
    NSString                *value, *tipText, *promptText, *viewTitle;
    BOOL                    requiresNumericInput;
    BOOL                    isPassword;
    
    NSString                *errText;

    // Formfield or rowKey, for the delegate to know which object it is editing
    NSObject                *context;  
    id<TextEditDelegate>	__weak _delegate;
    
    NumberKeypad            *numberKeyPad;
}
 
@property (strong, nonatomic) IBOutlet UITextField         *txt;
@property (strong, nonatomic) IBOutlet UILabel             *lblTip;
@property (strong, nonatomic) IBOutlet UILabel             *lblErr;
@property (strong, nonatomic) IBOutlet UIButton            *btnBack;

@property (strong, nonatomic) NSString                      *value;
@property (strong, nonatomic) NSString                      *tipText;
@property (strong, nonatomic) NSString                      *errText;
@property (strong, nonatomic) NSString                      *promptText;
@property (strong, nonatomic) NSString                      *viewTitle;

@property (strong, nonatomic) NSObject                      *context;
@property (weak, nonatomic) id<TextEditDelegate>			delegate;
@property BOOL requiresNumericInput;
@property BOOL isPassword;
@property BOOL disableAutoCorrect;

@property (nonatomic, strong) NumberKeypad                  *numberKeyPad;

- (void)setSeedData:(NSString*)val context:(NSObject*)context 
    delegate:(id<TextEditDelegate>) del
                tip:(NSString*) tip title:(NSString*)vTitle prompt:(NSString*)prompt 
          isNumeric:(BOOL)isNumeric isPassword:(BOOL)fPwd err:(NSString*)err;

@end
