//
//  SearchResultRouteView.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "JPTUtils.h"
#import "SearchResultRouteView.h"
#import "Segment.h"
#import "StationTableCellView.h"
#import "UIColor+JPT.h"

@implementation SearchResultRouteView

- (void)didMoveToSuperview {
    [super didMoveToSuperview];
    
    CGFloat tableHeight = 0.0f;
    
    NSUInteger numRows = [self.route.segments count];
    
    for (int i = 0; i < numRows; i ++) {
        tableHeight += [self tableView:self.tableView
               heightForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:0]];
    }
    
    tableHeight += 58;
    
    self.tableView.frame = CGRectMake(self.tableView.frame.origin.x,
                                      self.tableView.frame.origin.y,
                                      self.tableView.frame.size.width,
                                      tableHeight);
}

- (id)initWithCoder:(NSCoder *)decoder {
    self = [super initWithCoder:decoder];

    if (self) {
        [[[NSBundle mainBundle] loadNibNamed:@"SearchResultRouteView" owner:self options:nil] objectAtIndex:0];
        
        [self addSubview:self.view];
        
        self.duration.textColor = [UIColor cxBlack];
        self.fare.textColor = [UIColor cxBlack];
    }
    
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];

    if (self) {
        [[[NSBundle mainBundle] loadNibNamed:@"SearchResultRouteView" owner:self options:nil] objectAtIndex:0];
        
        [self addSubview:self.view];
        
        self.duration.textColor = [UIColor cxBlack];
        self.fare.textColor = [UIColor cxBlack];
    }
    
    return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
    CGFloat width = self.view.frame.size.width;
    CGFloat tableHeight = 0.0f;
    
    NSUInteger numRows = [self.route.segments count];
    
    for (int i = 0; i < numRows; i ++) {
        tableHeight += [self tableView:self.tableView
               heightForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:0]];
    }
    
    self.tableView.frame = CGRectMake(self.tableView.frame.origin.x,
                                      self.tableView.frame.origin.y,
                                      self.tableView.frame.size.width,
                                      tableHeight);
    
    // TODO: Hack to size it. Magic numbers everywhere. Make this dynamic.
    //
    return CGSizeMake(width, tableHeight + 69);
}

- (BOOL)isLastRow:(NSIndexPath *)indexPath {
    return [indexPath row] == [self.route.segments count];
}

#pragma mark - Cell factories

- (StationTableCellView *)cellForIndexPath:(NSIndexPath *)indexPath {
    StationTableCellView *v;
    
    if ([indexPath row] == 0) {
        v = [self firstStation];
    } else if ([self isLastRow:indexPath]) {
        v = [self lastStation];
    } else {
        v = [self throughStationAt:indexPath];
    }
    
    return v;
}

- (StationTableCellView *)firstStation {
    StationTableCellView *v = [[StationTableCellView alloc] initWithNib:@"FirstStationView"];
    
    v.frame = CGRectMake(0, 0, self.tableView.frame.size.width, 58);
    
    Segment *segment = [self.route.segments objectAtIndex:0];
    
    v.stationName.text = segment.fromStation.name;
    v.lineName.text = [NSString stringWithFormat:@"%@ / %@",
                       segment.line.name,
                       [JPTUtils labelForMinutes:segment.minutes]];
    v.price.text = [JPTUtils labelForFare:[segment totalCharge]];
    
    return v;
}

- (StationTableCellView *)lastStation {
    StationTableCellView *v = [[StationTableCellView alloc] initWithNib:@"LastStationView"];
    
    v.frame = CGRectMake(0, 0, self.tableView.frame.size.width, 26);
    
    Segment *segment = [self.route.segments lastObject];
    
    v.stationName.text = segment.toStation.name;
    
    return v;
}

- (StationTableCellView *)throughStationAt:(NSIndexPath *)indexPath {
    StationTableCellView *v = [[StationTableCellView alloc] initWithNib:@"ThroughStationView"];
    
    v.frame = CGRectMake(0, 0, self.tableView.frame.size.width, 61);
    
    NSUInteger throughStationIndex = [indexPath row];
    
    Segment *segment = [[self.route segments] objectAtIndex:throughStationIndex];
    
    v.stationName.text = segment.fromStation.name;
    v.lineName.text = [NSString stringWithFormat:@"%@ / %@",
                       segment.line.name,
                       [JPTUtils labelForMinutes:segment.minutes]];
    v.price.text = [self fareForSegment:segment];
    
    return v;
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"StationCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"StationCell"];
    }
    
    StationTableCellView *v = [self cellForIndexPath:indexPath];
    
    [cell.contentView addSubview:v];
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.route.segments count] + 1;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    CGFloat cellHeight = 0;
    
    if ([self isLastRow:indexPath]) {
        cellHeight = 26;
    } else {
        cellHeight = 58;
    }
    
    return cellHeight;
}

#pragma mark - Business logic

- (NSString *)fareForSegment:(Segment *)segment {
    NSString *fare = nil;
    NSString *fareLabel = [JPTUtils labelForFare:segment.fare];
    NSString *additionalChargeLabel = [JPTUtils labelForFare:segment.additionalCharge];
    
    if (fareLabel && additionalChargeLabel) {
        fare = [NSString stringWithFormat:@"%@ + %@",
                fareLabel, additionalChargeLabel];
    } else if (fareLabel) {
        fare = fareLabel;
    } else if (additionalChargeLabel) {
        fare = additionalChargeLabel;
    }
    
    return fare;
}

- (void)setRoute:(Route *)route {
    _route = route;
    
    self.fare.text = [JPTUtils labelForFare:route.fare];
    self.duration.text = [JPTUtils labelForMinutes:route.minutes];
}

@end
