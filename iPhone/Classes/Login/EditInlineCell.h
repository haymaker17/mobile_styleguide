//
//  EditInlineCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "LoginViewController.h"

@interface EditInlineCell : UITableViewCell <UITextFieldDelegate>{
    UITextField         *txt;
    MobileViewController *__weak parentVC;
    int                 rowPos;
}
@property int rowPos;
@property (strong, nonatomic) IBOutlet UITextField *txt;
@property (weak, nonatomic) MobileViewController *parentVC; // Do not use retain because parentVC retains the cell, and circular retains (where the cell also retains parentVC) will prevent either from ever getting released.

-(IBAction) textEdited:(id)sender;
-(IBAction)scrollMeUp:(id)sender;
@end
