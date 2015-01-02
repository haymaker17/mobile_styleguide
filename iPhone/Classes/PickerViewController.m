//
//  PickerViewController.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 17/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "PickerViewController.h"

@interface PickerViewController ()

@end

@implementation PickerViewController

- (instancetype)initWithTitle:(NSString *)title
{
    PickerViewController *pvc = [[UIStoryboard storyboardWithName:@"TravelPoints" bundle:nil] instantiateViewControllerWithIdentifier:@"PickerViewController"];
    pvc.title = title;
    return pvc;
}

- (instancetype)initAsPopover
{
    PickerViewController *pvc = [[UIStoryboard storyboardWithName:@"TravelPoints" bundle:nil] instantiateViewControllerWithIdentifier:@"PickerViewControllerPopover"];
    return pvc;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    [self.pickerView selectRow:self.pickerViewSelectedOptionIndex inComponent:0 animated:YES];
    self.lblSelectedText.text = self.pickerViewOptionLabels[self.pickerViewSelectedOptionIndex];
    
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {// If iOS 7
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
}

#pragma mark UIPickerDataSource methods

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    return [self.pickerViewOptionLabels count];
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return (NSString*)self.pickerViewOptionLabels[row];
}

#pragma mark UIPickerViewDelegate methods

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    self.lblSelectedText.text = self.pickerViewOptionLabels[row];
    if (self.pickerViewSelectedOptionIndex != row)
    {
        self.pickerViewSelectedOptionIndex = (int) row;
        [self.delegate pickerSelectionChangedToRow:self.pickerViewSelectedOptionIndex tag:self.tag];
    }
}

@end
