//
//  EmergencyContactVC.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/22/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEProfileEmergencyContact.h"

@interface EmergencyContactVC : UITableViewController <UITextFieldDelegate>

@property (nonatomic,strong) NSMutableDictionary *emergencyDict;
@property (strong, nonatomic) IBOutlet UIButton *btnChoose;

- (IBAction)btnClose:(id)sender;
- (IBAction)btnDone:(id)sender;

- (IBAction)switchToggle:(id)sender;
- (IBAction)btnChooseClick:(id)sender;

@end
