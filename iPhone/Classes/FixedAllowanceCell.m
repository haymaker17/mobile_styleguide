//
//  FixedAllowanceCell.m
//  ConcurMobile
//
//  Created by Wes Barton on 2/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FixedAllowanceCell.h"
#import "FixedAllowance.h"

@implementation FixedAllowanceCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

+ (NSString *)getMealProvidedValueLabel:(NSString *)provided {
    NSString *label = @"Default";
    if([provided isEqualToString:@"NPR"])
    {
        label = [Localizer getLocalizedText:@"Not Provided"];
    }
    else if ([provided isEqualToString:@"PRO"])
    {
        label = [Localizer getLocalizedText:@"Provided"];
    }
    else if ([provided isEqualToString:@"TAX"])
    {
        label = [Localizer getLocalizedText:@"Taxable"];
    }
    return label;
}

- (NSString *)getLodgingTypeValueLabel:(NSString *)value {
    NSString *label = [self.allowanceControl.lodgingTypeDictionary valueForKey:value];
    return label;
}


@synthesize mealAllowancePickerView;

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    if(self.lodgingTypePickerView != nil)
    {
        return [self.allowanceControl.lodgingTypeValues count];
    }

    return 3;
}



- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    if(self.lodgingTypePickerView != nil)
    {
        if(self.allowanceControl.lodgingTypeGermany)
        {
            if (row == 0) {
                return [self getLodgingTypeValueLabel:@"YRCPT"];
            }
            else if (row == 1) {
                return [self getLodgingTypeValueLabel:@"NNONE"];
            }
            else if (row == 2) {
                return [self getLodgingTypeValueLabel:@"NORCT"];
            }
        }
        else {
            if (row == 0) {
                return [self getLodgingTypeValueLabel:@"HOTEL"];
            }
            else if (row == 1) {
                return [self getLodgingTypeValueLabel:@"NCOOK"];
            }
            else if (row == 2) {
                return [self getLodgingTypeValueLabel:@"YCOOK"];
            }
        }
    }
    else
    {
        if(row == 0)
        {
            return [FixedAllowanceCell getMealProvidedValueLabel:@"NPR"];
        }
        else if (row == 1)
        {
            return [FixedAllowanceCell getMealProvidedValueLabel:@"PRO"];
        }
        else if (row == 2)
        {
            return [FixedAllowanceCell getMealProvidedValueLabel:@"TAX"];
        }
    }
    return @"Error";
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    if(self.lodgingTypePickerView != nil)
    {
        if(self.onLodgingTypeSelected)
        {
            if(self.allowanceControl.lodgingTypeGermany)
            {
                if (row == 0) {
                    self.onLodgingTypeSelected(@"YRCPT");
                }
                else if (row == 1) {
                    self.onLodgingTypeSelected(@"NNONE");
                }
                else if (row == 2) {
                    self.onLodgingTypeSelected(@"NORCT");
                }
            }
            else {

                if (row == 0) {
                    self.onLodgingTypeSelected(@"HOTEL");
                }
                else if (row == 1) {
                    self.onLodgingTypeSelected(@"NCOOK");
                }
                else if (row == 2) {
                    self.onLodgingTypeSelected(@"YCOOK");
                }
            }
        }
    }
    else
    {
        if (self.onMealAllowanceSelected) {
            if(row == 0)
            {
                self.onMealAllowanceSelected(@"NPR");
            }
            else if (row == 1)
            {
                self.onMealAllowanceSelected(@"PRO");
            }
            else if (row == 2)
            {
                self.onMealAllowanceSelected(@"TAX");
            }
        }
    }
}


-(void) inputAccessoryViewDidFinish
{
    [self.mealSelectedValue resignFirstResponder];
}

-(void) inputAccessoryViewDidFinishLodgingType
{
    [self.lodgingTypeValue resignFirstResponder];
}

-(void) inputAccessoryViewDidFinishBreakfastAmountText
{
    [self.breakfastAmountText resignFirstResponder];

}

-(void) inputAccessoryViewDidFinishBreakfastExchangeRateText
{
    [self.breakfastExchangeRateText resignFirstResponder];
}


+ (void)setMealAllowancePickerDefault:(FixedAllowance *)allowance cell:(FixedAllowanceCell *)cell provided:(NSString *)provided {
    if([provided isEqualToString:@"NPR"])
    {
        [cell.mealAllowancePickerView selectRow:0 inComponent:0 animated:NO];
    }
    else if([provided isEqualToString:@"PRO"])
    {
        [cell.mealAllowancePickerView selectRow:1 inComponent:0 animated:NO];
    }
    else if([provided isEqualToString:@"TAX"])
    {
        [cell.mealAllowancePickerView selectRow:2 inComponent:0 animated:NO];
    }
}

- (void)setLodgingTypePickerDefault:(NSString *)selected
{
    for(int i=0; i < [self.allowanceControl.lodgingTypeValues count]; i++)
    {
        if([selected isEqualToString:[self.allowanceControl.lodgingTypeValues objectAtIndex:i]])
        {
            [self.lodgingTypePickerView selectRow:i inComponent:0 animated:YES];
        }
    }
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component {
    return 30;
}


@end
