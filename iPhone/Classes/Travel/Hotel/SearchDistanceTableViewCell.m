//
//  SearchDistanceTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SearchDistanceTableViewCell.h"

@interface SearchDistanceTableViewCell ()
@property (nonatomic, strong) NSDictionary *distanceValuesAndUnit;
@end

@implementation SearchDistanceTableViewCell

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
    SearchDistanceCellData *distanceData = [[SearchDistanceCellData alloc] init];
    self.distanceValuesAndUnit = distanceData.searchDistanceOptions;
    self.distancePicker.delegate = self;
    self.distancePicker.dataSource = self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

#pragma mark - pickerView data source
-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return [self.distanceValuesAndUnit count];
}

-(NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    if (component == 0) {
            return [[self.distanceValuesAndUnit objectForKey:@"distanceValues"] count];
    }
    return 1;
}

-(NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    if (component == 0) {
        return [self.distanceValuesAndUnit objectForKey:@"distanceValues"][row];
    }
    return [self.distanceValuesAndUnit objectForKey:@"distanceUnit"];
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component;
{
    NSString *selectedValue = [self.distanceValuesAndUnit objectForKey:@"distanceValues"][row];
    NSString *distance = [NSString stringWithFormat:@"%@ %@", selectedValue, [self.distanceValuesAndUnit objectForKey:@"distanceUnit"]];
    
    if (self.hotelSearchDistanceDidChanged) {
        self.hotelSearchDistanceDidChanged(distance);
    }
}

@end
