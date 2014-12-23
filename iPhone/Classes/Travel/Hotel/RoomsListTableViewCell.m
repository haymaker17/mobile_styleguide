//
//  RoomsListTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/6/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RoomsListTableViewCell.h"

@implementation RoomsListTableViewCell

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

- (void)disableAndFadeCell
{
    [self setUserInteractionEnabled:NO];
    [self.labelRoomRate setAlpha:0.4];
    [self.labelRoomDescription setAlpha:0.4];
    [self.labelDepositRequired setAlpha:0.4];
    [self.labelPerNight setAlpha:0.4];

    [self.labelOutOfPolicy setHidden:NO];
    [self.triangleBadge setHidden:YES];
}

- (void)enableAndUnfadeCell
{
    [self setUserInteractionEnabled:YES];
    [self.labelRoomRate setAlpha:1.0];
    [self.labelRoomDescription setAlpha:1.0];
    [self.labelDepositRequired setAlpha:1.0];

    [self.labelOutOfPolicy setHidden:YES];
    [self.triangleBadge setHidden:YES];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(RoomsListCellData *)cellData
{
    CTEHotelRate *hotelRatesData = [cellData getHotelRatesData];
    self.labelRoomDescription.text = hotelRatesData.roomDescription;

    self.labelRoomRate.text = [FormatUtils formatMoneyString:hotelRatesData.dailyAmount crnCode:hotelRatesData.currency decimalPlaces:0];
    
    // check if deposit is required
    if (![hotelRatesData.guaranteeSurcharge isEqualToString:@"DepositRequired"]) {
        [self.labelDepositRequired setHidden:YES];
    } else {
        [self.labelDepositRequired setHidden:NO];
    }

    [self handleViolations:hotelRatesData];
}

- (void)handleViolations:(CTEHotelRate *)rate
{
    [self enableAndUnfadeCell];

    if (rate.mostSevereViolation) {
        switch (rate.mostSevereViolation.enforcementLevel) {
            case CTEHotelBookingAllowed:
                // This means the violation is just informational
                // There are no UI changes at this level
                break;

            case CTEHotelBookingAllowedWithViolationCode:
                [self.triangleBadge setHidden:NO];
                [self.triangleBadge switchToYellow];
                break;

            case CTEHotelBookingAllowedWithViolationCodeAndApproval:
                [self.triangleBadge setHidden:NO];
                [self.triangleBadge switchToRed];
                break;

            case CTEHotelBookingNotAllowed:
                [self disableAndFadeCell];
                break;
        }
    }
}

// format the amount with currency code
//-(NSString *)getRoomRateWithCurrencyCode:(NSString *)currencyCode amount:(double)amount
//{
//    NSString *roomRate = nil;
//    NSString *amountStr = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", amount] crnCode:currencyCode];
//    NSRange range = [amountStr rangeOfString:@"."];
//    if (range.location != NSNotFound) {
//        roomRate = [amountStr substringToIndex:range.location];
//    }
//    return roomRate;
//}

@end
