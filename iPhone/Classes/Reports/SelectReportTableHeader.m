//
//  SelectReportTableHeader.m
//  ConcurMobile
//
//  Created by ernest cho on 9/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "SelectReportTableHeader.h"

@interface SelectReportTableHeader()
@property (nonatomic, readwrite, strong) IBOutlet UIView *toplevelSubView;
@property (nonatomic, readwrite, strong) IBOutlet UILabel *helpText;

@property (nonatomic, readwrite, strong) IBOutlet UILabel *smartMatchingText;
@property (nonatomic, readwrite, strong) IBOutlet UILabel *performanceWarningText;
@property (nonatomic, readwrite, strong) IBOutlet UISwitch *smartMatchSwitch;

@property (nonatomic, readwrite, assign) BOOL isEnabled;
@end

@implementation SelectReportTableHeader

// need the frame size to init with correct sizing
- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [[NSBundle mainBundle] loadNibNamed:@"SelectReportTableHeader" owner:self options:nil];
        [self addSubview:self.toplevelSubView];

        // set the help text
        self.helpText.text = [Localizer getLocalizedText:@"SELECT_REPORT_HELP"];
        self.smartMatchingText.text = [Localizer getLocalizedText:@"Smart Matching"];
        self.performanceWarningText.text = [Localizer getLocalizedText:@"Adds processing time"];

        self.isEnabled = [[ExSystem sharedInstance].entitySettings.smartExpenseEnabledOnReports boolValue];
        [self.smartMatchSwitch setOn:self.isEnabled];
        
        [self.smartMatchSwitch addTarget:self action:@selector(smartMatchSwitchChanged) forControlEvents:UIControlEventValueChanged];
    }
    return self;
}

- (void)smartMatchSwitchChanged
{
    if (self.isEnabled != self.smartMatchSwitch.on) {
        self.isEnabled = self.smartMatchSwitch.on;
        [ExSystem sharedInstance].entitySettings.smartExpenseEnabledOnReports = [NSNumber numberWithBool:self.isEnabled];
        [[ExSystem sharedInstance] saveSettings];

        NSDictionary *dict = @{@"From": @"Select Report Screen"};
        if (self.isEnabled) {
            [Flurry logEvent:@"SmartExpense: Report Match On" withParameters:dict];
        } else {
            [Flurry logEvent:@"SmartExpense: Report Match Off" withParameters:dict];
        }
    }
}

@end
