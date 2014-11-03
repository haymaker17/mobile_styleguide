//
//  ItineraryStopDetailDateViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 3/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ItineraryStopDetailDateViewController : UIViewController

@property NSDate *workingDate;
@property NSString *whichDate;
//@property (weak, nonatomic) IBOutlet UIDatePicker *workingDatePicker;
@property (weak, nonatomic) IBOutlet UIDatePicker *workingTimePicker;

@end
