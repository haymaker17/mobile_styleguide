//
//  FancyDatePickerView.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "FancyDatePickerView.h"

@implementation FancyDatePickerView

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
        [[[NSBundle mainBundle] loadNibNamed:@"FancyDatePickerView" owner:self options:nil] objectAtIndex:0];
        
        [self addSubview:self.view];
    }
    
    return self;
}

- (IBAction)didChangeDate:(id)sender {
    if ([self.delegate respondsToSelector:@selector(datePicker:didChangeDate:)]) {
        [self.delegate datePicker:self didChangeDate:[self.picker date]];
    }
}

- (IBAction)didDismiss:(id)sender {
    if ([self.delegate respondsToSelector:@selector(datePickerDidDismiss:)]) {
        [self.delegate datePickerDidDismiss:self];
    }
}

@end
