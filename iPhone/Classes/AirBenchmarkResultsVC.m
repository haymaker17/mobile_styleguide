//
//  AirBenchmarkResultsVC.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 13/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AirBenchmarkResultsVC.h"
#import "UserConfig.h"

@interface AirBenchmarkResultsVC ()

@end

@implementation AirBenchmarkResultsVC

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {// If iOS 7
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    self.title = [@"Search Results" localize];
    
    // Resize header label in viewWillAppear. sizeToFit doesn't work properly here
    self.lblHeader.attributedText = [self getHeaderAttributedText];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.lblHeader sizeToFit];
    CGFloat tableHeightOffset = self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8 - self.tableView.frame.origin.y;
    self.tableView.frame = CGRectMake(self.tableView.frame.origin.x, self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8, self.tableView.frame.size.width, self.tableView.frame.size.height - tableHeightOffset);
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 1;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"LeftDetailCellForAirBenchmark";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:cellIdentifier];
    }
    if ([self.benchmarkData.price doubleValue] != 0) {
        cell.textLabel.text = [FormatUtils formatMoneyWithNumber:self.benchmarkData.price crnCode:self.benchmarkData.crnCode];
        cell.textLabel.font = [UIFont systemFontOfSize:16];
        cell.textLabel.textColor = [UIColor blackColor];
    }
    else
    {
        cell.textLabel.text = [@"Unavailable" localize];
        cell.textLabel.font = [UIFont systemFontOfSize:12];
        cell.textLabel.textColor = [UIColor grayColor];
    }
    
    cell.detailTextLabel.text = [[self.toAirportFullName componentsSeparatedByString:@","] objectAtIndex:0];
    cell.detailTextLabel.font = [UIFont systemFontOfSize:14];
    cell.detailTextLabel.minimumScaleFactor = 0.6;
    cell.textLabel.minimumScaleFactor = 0.6;
    cell.textLabel.adjustsFontSizeToFitWidth = YES;
    cell.detailTextLabel.adjustsFontSizeToFitWidth = YES;
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 1;
}

-(NSAttributedString *)getHeaderAttributedText
{
    NSMutableAttributedString *text = [[NSMutableAttributedString alloc] init];
    NSDictionary *attributes = @{NSFontAttributeName : [UIFont fontWithName:@"HelveticaNeue" size:14], NSForegroundColorAttributeName : [UIColor blackColor]};
    //NSDictionary *newlineAttributes = @{NSFontAttributeName : [UIFont fontWithName:@"HelveticaNeue" size:8], NSForegroundColorAttributeName : [UIColor blackColor]};
    NSDictionary *headingAttributes = @{NSFontAttributeName : [UIFont boldSystemFontOfSize:14], NSForegroundColorAttributeName : [UIColor blackColor]};
    
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n\n",[self getConfigBasedInitialMessage]] attributes:attributes]];
    
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@:",[@"Departure" localize]] attributes:headingAttributes]];
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@" %@\n",self.fromAirportFullName] attributes:attributes]];
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@:",[@"ITIN_DETAILS_VIEW_ARRIVAL" localize]] attributes:headingAttributes]];
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@" %@\n",self.toAirportFullName] attributes:attributes]];
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@:", [@"Departure Date" localize]] attributes:headingAttributes]];
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@" %@\n\n",[DateTimeFormatter formatDateForBooking:self.benchmarkData.date  TimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]]] attributes:attributes]];
    NSString *footerText = [@"Airfare Price to Beat" localize];
    if (self.benchmarkData.roundtrip)
        footerText = [footerText stringByAppendingFormat:@" (%@)",[@"Round Trip" localize]];
    footerText = [footerText uppercaseStringWithLocale:[NSLocale currentLocale]];
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:footerText attributes:attributes]];
    
    return text;
}

-(NSString *)getConfigBasedInitialMessage
{
    if([[UserConfig getSingleton].travelPointsConfig[@"AirTravelPointsEnabled"] boolValue])
        return [@"PRICE_TO_BEAT_AIR_RESULTS_HEADER" localize];
    else
        return [@"PRICE_TO_BEAT_DISABLED_AIR_RESULTS_HEADER" localize];
}

@end
