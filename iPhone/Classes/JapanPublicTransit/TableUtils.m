//
//  TableUtils.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "DCRoundSwitch.h"
#import "Localizer.h"
#import "TableUtils.h"
#import "UIColor+JPT.h"

@implementation TableUtils

+ (UITableViewCell *)disclosureCellWithLabel:(NSString *)label
                                    forTable:(UITableView *)tableView {
    
    return [TableUtils disclosureCellWithLabel:label andDetail:nil forTable:tableView required:NO];
}

+ (UITableViewCell *)disclosureCellWithLabel:(NSString *)label
                                   andDetail:(NSString *)detail
                                    forTable:(UITableView *)tableView {
    
    return [TableUtils disclosureCellWithLabel:label andDetail:detail forTable:tableView required:NO];
}

+ (UITableViewCell *)disclosureCellWithLabel:(NSString *)label
                                   andDetail:(NSString *)detail
                                    forTable:(UITableView *)tableView
                                    required:(BOOL)required {
    
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"DisclosureCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                      reuseIdentifier:@"DisclosureCell"];
    }
    
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    cell.backgroundColor = [UIColor whiteColor];
    cell.detailTextLabel.text = detail;
    cell.textLabel.textColor = [UIColor cxBlack];
    
    if (required) {
        cell.textLabel.text = [NSString stringWithFormat:@"%@ *", label];
        
        if (detail == nil) {
            cell.textLabel.textColor = [UIColor redColor];
        }
    } else {
        cell.textLabel.text = label;
    }
    
    return cell;
}

+ (BOOL)isSwitchControl:(id)sender {
    BOOL isSwitch = NO;
    
    if ([sender isKindOfClass:DCRoundSwitch.class]) {
        isSwitch = YES;
    } else if ([sender isKindOfClass:UISwitch.class]) {
        isSwitch = YES;
    }
    
    return isSwitch;
}

+ (BOOL)switchValue:(id)sender {
    BOOL value = NO;
    
    if ([sender isKindOfClass:DCRoundSwitch.class]) {
        DCRoundSwitch *toggle = (DCRoundSwitch *)sender;
        
        value = toggle.on;
    } else if ([sender isKindOfClass:UISwitch.class]) {
        UISwitch *s = (UISwitch *) sender;
        
        value = s.on;
    }
    
    return value;
}

+ (UITableViewCell *)toggleCellWithLabel:(NSString *)label
                                forTable:(UITableView *)tableView
                               withValue:(BOOL)val
                                onTarget:(id)target
                             andSelector:(SEL)selector {
    
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"ToggleCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                      reuseIdentifier:@"ToggleCell"];
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.backgroundColor = [UIColor whiteColor];
    }
    
    UIControl *toggle;
    
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
        toggle = [[UISwitch alloc] init];
        
        UISwitch *toggleImpl = (UISwitch *) toggle;
        
        [toggleImpl addTarget:target action:selector forControlEvents:UIControlEventValueChanged];
        
        [toggleImpl setOn:val];
    } else {
        toggle = [[DCRoundSwitch alloc] init];
        
        DCRoundSwitch *toggleImpl = (DCRoundSwitch *) toggle;
        
        [toggleImpl addTarget:target action:selector forControlEvents:UIControlEventValueChanged];
        
        toggleImpl.onText = [Localizer getLocalizedText:@"Yes"];
        toggleImpl.offText = [Localizer getLocalizedText:@"No"];
        
        [toggleImpl setOn:val animated:NO ignoreControlEvents:YES];
    }
    
    cell.textLabel.text = label;
    cell.detailTextLabel.text = nil;
    cell.accessoryView = toggle;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    return cell;
}

@end
