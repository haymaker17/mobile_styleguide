//
//  PickerViewController.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 17/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PickerDelegate.h"

@interface PickerViewController : UIViewController <UIPickerViewDataSource, UIPickerViewDelegate>

@property (strong, nonatomic) NSArray *pickerViewOptionLabels;
@property (nonatomic) int pickerViewSelectedOptionIndex;
@property (strong, nonatomic) id tag;
@property (strong, nonatomic) id<PickerDelegate> delegate;

@property (strong, nonatomic) IBOutlet UILabel *lblSelectedText;
@property (strong, nonatomic) IBOutlet UIPickerView *pickerView;

- (instancetype)initWithTitle:(NSString *)title;
- (instancetype)initAsPopover;

@end
