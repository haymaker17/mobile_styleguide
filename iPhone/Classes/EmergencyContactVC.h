//
//  EmergencyContactVC.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/22/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEProfileEmergencyContact.h"

@interface EmergencyContactVC : UITableViewController <UITextFieldDelegate,UIAlertViewDelegate>

@property (nonatomic,strong) NSMutableDictionary *emergencyDict;

- (IBAction)btnClose:(id)sender;
- (IBAction)btnDone:(id)sender;

- (IBAction)switchToggle:(id)sender;
- (IBAction)btnChooseClick:(id)sender;

@property (nonatomic,strong) void(^onSelected)(NSString *name);     // For Record the Name

@end
