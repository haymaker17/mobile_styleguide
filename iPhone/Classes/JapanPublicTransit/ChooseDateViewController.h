//
//  ChooseDateViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChooseDateViewController : UIViewController

@property (strong, nonatomic) NSDate *date;
@property (weak, nonatomic) IBOutlet UIDatePicker *datePicker;

- (IBAction)dateChanged:(id)sender;

@end
