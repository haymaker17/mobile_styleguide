//
//  ItineraryCell.m
//  ConcurMobile
//
//  Created by Wes Barton on 4/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <sys/select.h>
#import "ItineraryCell.h"
#import "Itinerary.h"
#import "ItineraryConfig.h"

@implementation ItineraryCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)awakeFromNib
{
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

//@synthesize tripLengthPickerView;

+ (void)setTripLengthPickerDefault:(Itinerary *)itinerary cell:(ItineraryCell *)cell selectedValue:(NSString *)selectedValue
{
//    NSLog(@"setTripLengthPickerDefault::::selectedValue = %@", selectedValue);
    if(selectedValue != nil) {
        for (int i = 0; i < [cell.itineraryConfig.tripLengthListKeys count]; ++i) {
            NSString *key = (NSString *) [cell.itineraryConfig.tripLengthListKeys objectAtIndex:i];
            if ([key isEqualToString:selectedValue]) {
                [cell.tripLengthPickerView selectRow:i inComponent:0 animated:NO];
                break;
            }
        }
    }
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    NSUInteger numberOfTripLengthOptions = [self.itineraryConfig.tripLengthListKeys count];
    return numberOfTripLengthOptions;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    NSString *rowKey = [NSString stringWithFormat:@"%li",row + 1];
    NSString *valueForKey = [self.itineraryConfig.tripLengthListValues valueForKey:rowKey];
//    NSLog(@"valueForKey(%i) = %@", row, valueForKey);
    return valueForKey;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
//    NSString *rowKey = [NSString stringWithFormat:@"%i",row + 1];
    NSString *rowKey = [self.itineraryConfig.tripLengthListKeys objectAtIndex:row];
//    NSLog(@"rowKey From list = %@", rowKey);
    self.onTripLengthSelected(rowKey);
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component {
    return 30;
}

-(void) inputAccessoryViewDidFinish
{
    [self.tripLengthValue resignFirstResponder];
}

+ (void)composeItineraryDateRange:(Itinerary *)itinerary cell:(ItineraryCell *)cell format:(NSString *)format
{
    cell.itineraryDateRange.text = @"";

    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:format];

    ItineraryStop *firstStop = itinerary.stops.firstObject;
    if(firstStop != nil) {
        NSString *firstDepartureDateText = [formatter stringFromDate:firstStop.departureDate];
        ItineraryStop *lastStop = itinerary.stops.lastObject;
        if(lastStop != nil)
        {
            NSString *lastArrivalDateText = [formatter stringFromDate:lastStop.arrivalDate];
            if ([firstDepartureDateText isEqualToString:lastArrivalDateText]) {
                cell.itineraryDateRange.text = firstDepartureDateText;
            }
            else {
                cell.itineraryDateRange.text = [NSString stringWithFormat:@"%@ - %@", firstDepartureDateText, lastArrivalDateText];
            }

        } else{
            cell.itineraryDateRange.text = firstDepartureDateText;
        }

    } else{
        cell.itineraryDateRange.text = @"";
    }
}

-(void) inputAccessoryViewDidFinishinputDidItineraryName
{
    [self.itineraryNameEdit resignFirstResponder];
}



@end
